package dev.schlaubi.musicbot.core.plugin

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.config.Config
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.pf4j.*
import org.pf4j.update.DefaultUpdateRepository
import org.pf4j.update.UpdateManager
import org.pf4j.update.UpdateRepository
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.KClass

private val LOG = KotlinLogging.logger { }

object PluginLoader : DefaultPluginManager(), KoinComponent {
    internal val system: PluginSystem = DefaultPluginSystem(this)
    private val repos: List<UpdateRepository> = Config.PLUGIN_REPOSITORIES.map {
        DefaultUpdateRepository(
            generateNonce(), URL(it)
        )
    }
    private val updateManager = UpdateManager(this, repos)
    private val rootTranslations = ClassLoader.getSystemClassLoader().findTranslations()

    private val bot: ExtensibleBot by inject()
    private lateinit var pluginBundles: Map<String, String>
    private lateinit var pluginExtensions: Map<String, List<String>>
    lateinit var extensions: List<Extension>

    override fun getPluginDescriptorFinder(): PluginDescriptorFinder = MikBotPluginDescriptionFinder()

    override fun loadPlugins() {
        super.loadPlugins()

        checkForUpdates()
        buildTranslationGraph()
        buildExtensionGraph()
    }

    @OptIn(ExperimentalStdlibApi::class)
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

    private fun checkForUpdates() {
        if (repos.isEmpty()) {
            LOG.warn { "No plugin repositories are defined, Updater will disable itself" }
            return
        }

        if (updateManager.hasUpdates()) {
            val updates = updateManager.updates
            LOG.debug { "Found ${updates.size} plugin updates" }
            for (plugin in updates) {
                LOG.debug { "Found update for plugin '${plugin.id}'" }
                val lastRelease = updateManager.getLastPluginRelease(plugin.id)
                val lastVersion = lastRelease.version
                val installedVersion = getPlugin(plugin.id).descriptor.version

                LOG.debug { "Update plugin '${plugin.id}' from version $installedVersion to version $lastVersion" }
                val updated = updateManager.updatePlugin(plugin.id, lastVersion)
                if (updated) {
                    LOG.debug { "Updated plugin '${plugin.id}'" }
                } else {
                    LOG.error { "Cannot update plugin '${plugin.id}'" }
                }
            }
        } else {
            LOG.debug { "No updates found" }
        }

        if (updateManager.hasAvailablePlugins()) {
            val installedPlugins = plugins.values.map { it.pluginId }
            val requestedPlugins = Config.DOWNLOAD_PLUGINS
                .filter { it.id !in installedPlugins }
            val availablePlugins = updateManager.availablePlugins
                .associateBy { it.id }

            if (requestedPlugins.isNotEmpty()) {
                LOG.info { "Attempting to download the following plugins: $requestedPlugins" }
            }

            requestedPlugins.forEach {
                val plugin = availablePlugins[it.id]
                if (plugin == null) {
                    LOG.warn { "Could not find plugin ${it.id} in any repo" }
                    return@forEach
                }

                val version = it.version ?: updateManager.getLastPluginRelease(plugin.id).version

                val installed = updateManager.installPlugin(plugin.id, version)
                if (installed) {
                    LOG.info { "Successfully installed plugin ${plugin.id} from a repository" }
                } else {
                    LOG.info { "Installation for plugin ${plugin.id} failed" }
                }
            }
        }
    }

    private fun buildExtensionGraph() {
        val extensionMap = botPlugins.associate {
            val pluginBuilder = ExtensibleBotBuilder.ExtensionsBuilder()
            with(it) {
                pluginBuilder.addExtensions()
            }

            it.wrapper.pluginId to pluginBuilder.extensions.map { it() }
        }

        pluginExtensions = extensionMap.mapValues { (_, extensions) ->
            extensions.map { it.name }
        }
        LOG.debug { "Build plugin extension graph: $pluginExtensions" }
        extensions = extensionMap.values.flatten()
    }

    override fun unloadPlugin(pluginId: String, unloadDependents: Boolean): Boolean {
        return runBlocking {
            val extensionNames = pluginExtensions[pluginId]
            if (extensionNames != null) {
                LOG.debug { "Unloading extensions for plugin $pluginId: $extensionNames" }
                extensionNames.forEach {
                    bot.unloadExtension(it)
                }
            }
            super.unloadPlugin(pluginId, unloadDependents)
        }
    }

    fun getPluginForBundle(bundle: String): PluginWrapper? {
        val sanitizedName = bundle.substringAfter("translations.").substringBefore(".")
        val pluginName = pluginBundles[sanitizedName] ?: return null

        return getPlugin(pluginName)
    }

    val botPlugins: List<Plugin> get() = plugins.values.map { it.plugin.asPlugin() }
}

private class MikBotPluginDescriptionFinder : ManifestPluginDescriptorFinder() {
    override fun isApplicable(pluginPath: Path): Boolean = pluginPath.exists() && pluginPath.isDirectory()

    override fun getManifestPath(pluginPath: Path): Path {
        return pluginPath / "classes" / "META-INF" / "MANIFEST.MF"
    }
}

private class DefaultPluginSystem(private val manager: PluginManager) : PluginSystem {
    override fun <T : ExtensionPoint> getExtensions(type: KClass<T>): List<T> = manager.getExtensions(type.java)
}

private fun ClassLoader.findTranslations(): Sequence<String> {
    val resourcePath = getResource("translations")?.file

    val path = resourcePath?.let { path -> Path(path) }
    if (path != null && path.isDirectory()) {
        return path.listDirectoryEntries()
            .asSequence()
            .filter { it.isDirectory() }
            .map { it.name }
    }

    return emptySequence()
}
