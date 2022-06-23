package dev.schlaubi.musicbot.core.plugin

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.event.Event
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.util.ensurePath
import dev.schlaubi.musicbot.core.Bot
import io.ktor.util.*
import kotlinx.coroutines.flow.MutableSharedFlow
import mu.KotlinLogging
import org.pf4j.*
import org.pf4j.DependencyResolver.*
import org.pf4j.update.DefaultUpdateRepository
import org.pf4j.update.UpdateRepository
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.KClass

private val LOG = KotlinLogging.logger { }

object PluginLoader : DefaultPluginManager(), KordExKoinComponent {
    internal val repos: List<UpdateRepository> = Config.PLUGIN_REPOSITORIES.map {
        DefaultUpdateRepository(
            generateNonce(), URL(it)
        )
    }
    internal val updateManager = PluginUpdater(this, repos)
    private val rootTranslations = ClassLoader.getSystemClassLoader().findTranslations()
    override fun createExtensionFinder(): ExtensionFinder = DependencyCheckingExtensionFinder(this)
    val resolver: DependencyResolver get() = dependencyResolver
    private lateinit var pluginBundles: Map<String, String>

    override fun getPluginDescriptorFinder(): PluginDescriptorFinder = MikBotPluginDescriptionFinder()

    override fun createPluginsRoot(): List<Path> {
        // Load bundled plugins
        val bundledPlugins = ClassLoader.getSystemResource("bundled-plugins")
        return if (bundledPlugins != null) {
            listOf(*super.createPluginsRoot().toTypedArray(), Path.of(bundledPlugins.toURI()))
        } else {
            super.createPluginsRoot()
        }
    }

    override fun loadPlugins() {
        super.loadPlugins()

        updateManager.checkForUpdates()
        buildTranslationGraph()
    }

    private fun buildTranslationGraph() {
        pluginBundles = buildMap {
            plugins.values.forEach { plugin ->
                plugin.pluginClassLoader.findTranslations()
                    .filter { it !in rootTranslations }
                    .forEach {
                        this[it] = plugin.pluginId
                    }
            }
        }

        LOG.debug { "Built translation provider graph: $pluginBundles" }
    }

    override fun resolvePlugins() {
        // retrieves the plugins descriptors
        val descriptors = plugins.values.map { it.descriptor }
        val result = dependencyResolver.resolve(descriptors)
        if (result.hasCyclicDependency()) {
            // This is too fatal to recover
            throw CyclicDependencyException()
        }

        val sortedPluginWrappers = result.sortedPlugins.mapNotNull { plugins[it] }

        val notFoundDependencies = result.notFoundDependencies
        if (notFoundDependencies.isNotEmpty()) {
            val exception = DependenciesNotFoundException(notFoundDependencies)
            val missingDependencyPlugins =
                sortedPluginWrappers.findPluginsDependingOn(notFoundDependencies)

            missingDependencyPlugins.failPlugins(exception)

            LOG.warn(exception) { "Disabling the following plugins, because of a missing dependency: ${missingDependencyPlugins.map { it.descriptor.pluginId }}" }
        }
        val wrongVersionDependencies = result.wrongVersionDependencies
        if (wrongVersionDependencies.isNotEmpty()) {
            val exception = DependenciesWrongVersionException(wrongVersionDependencies)

            val dependencyConflictPlugins =
                sortedPluginWrappers.findPluginsDependingOn(
                    wrongVersionDependencies.map { it.dependencyId },
                    overrideOptional = true
                )

            dependencyConflictPlugins.failPlugins(exception)

            LOG.warn(exception) { "Disabling the following plugins, because of a wrong dependency: ${dependencyConflictPlugins.map { it.descriptor.pluginId }}" }
        }
        val sortedPlugins = result.sortedPlugins

        // move plugins from "unresolved" to "resolved"
        for (pluginId in sortedPlugins) {
            val pluginWrapper = plugins[pluginId]
            if (unresolvedPlugins.remove(pluginWrapper)) {
                val pluginState = pluginWrapper!!.pluginState
                if (pluginState != PluginState.DISABLED) {
                    pluginWrapper.pluginState = PluginState.RESOLVED
                }
                resolvedPlugins.add(pluginWrapper)
                LOG.info { "Plugin '${getPluginLabel(pluginWrapper.descriptor)}' resolved" }
                firePluginStateEvent(PluginStateEvent(this, pluginWrapper, pluginState))
            }
        }
    }

    private fun List<PluginWrapper>.failPlugins(
        exception: Throwable,
    ) {
        forEach {
            it.failedException = exception
            it.pluginState = PluginState.DISABLED
        }
    }

    private fun List<PluginWrapper>.findPluginsDependingOn(
        dependencies: List<String>,
        overrideOptional: Boolean = false,
    ) = filter {
        it.descriptor.dependencies.any { dependency ->
            (!dependency.isOptional && !overrideOptional) && dependency.pluginId in dependencies
        }
    }

    fun getPluginForBundle(bundle: String): PluginWrapper? {
        val sanitizedName = bundle.substringAfter("translations.").substringBefore(".")
        val pluginName = pluginBundles[sanitizedName] ?: return null

        return getPlugin(pluginName)
    }

    val botPlugins: List<Plugin>
        get() = plugins.values
            .asSequence()
            .filter {
                it.pluginState != PluginState.FAILED && it.pluginState != PluginState.STOPPED && it.pluginState != PluginState.DISABLED
            }
            // If the plugin loading fails there is no classloader, but not necessarily a specific state
            .filter { it.pluginClassLoader != null }
            .map { it.plugin.asPlugin() }
            .toList()
}

private class MikBotPluginDescriptionFinder : PluginDescriptorFinder {
    private val propertiesFinder = PropertiesPluginDescriptorFinder()
    private val manifestFinder = MikBotPluginManifestDescriptionFinder()

    override fun isApplicable(pluginPath: Path): Boolean = pluginPath.exists() && pluginPath.isDirectory()

    override fun find(pluginPath: Path): PluginDescriptor {
        val newPath = pluginPath / "plugin.properties"
        if (newPath.exists()) {
            return propertiesFinder.find(pluginPath)
        }

        return manifestFinder.find(pluginPath)
    }
}

private class MikBotPluginManifestDescriptionFinder : ManifestPluginDescriptorFinder() {
    override fun isApplicable(pluginPath: Path): Boolean = pluginPath.exists() && pluginPath.isDirectory()

    override fun getManifestPath(pluginPath: Path): Path {
        return pluginPath / "classes" / "META-INF" / "MANIFEST.MF"
    }
}

internal class DefaultPluginSystem(private val bot: Bot) : PluginSystem {
    internal val events = MutableSharedFlow<Event>()

    override fun <T : ExtensionPoint> getExtensions(type: KClass<T>): List<T> = PluginLoader.getExtensions(type.java)

    override fun translate(key: String, bundleName: String, locale: String?, replacements: Array<Any?>): String =
        bot.translationProvider.translate(key, bundleName, locale, replacements)

    override suspend fun emitEvent(event: Event) = events.emit(event)
}

private fun ClassLoader.findTranslations(): Sequence<String> {
    val resourcePath = getResource("translations")?.file?.ensurePath()

    val path = resourcePath?.let(::Path)
    if (path != null && path.isDirectory()) {
        return path.listDirectoryEntries()
            .asSequence()
            .filter { it.isDirectory() }
            .map(Path::name)
    }

    return emptySequence()
}
