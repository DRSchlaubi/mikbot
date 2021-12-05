package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.util.ensurePath
import dev.schlaubi.musicbot.core.Bot
import io.ktor.util.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
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
    private val repos: List<UpdateRepository> = Config.PLUGIN_REPOSITORIES.map {
        DefaultUpdateRepository(
            generateNonce(), URL(it)
        )
    }
    private val updateManager = UpdateManager(this, repos)
    private val rootTranslations = ClassLoader.getSystemClassLoader().findTranslations()
    override fun createExtensionFinder(): ExtensionFinder = DependencyCheckingExtensionFinder(this)

    private lateinit var pluginBundles: Map<String, String>

    override fun getPluginDescriptorFinder(): PluginDescriptorFinder = MikBotPluginDescriptionFinder()

    override fun loadPlugins() {
        super.loadPlugins()

        checkForUpdates()
        buildTranslationGraph()
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

    fun getPluginForBundle(bundle: String): PluginWrapper? {
        val sanitizedName = bundle.substringAfter("translations.").substringBefore(".")
        val pluginName = pluginBundles[sanitizedName] ?: return null

        return getPlugin(pluginName)
    }

    val botPlugins: List<Plugin> get() = plugins.values.map { it.plugin.asPlugin() }
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
    override fun <T : ExtensionPoint> getExtensions(type: KClass<T>): List<T> = PluginLoader.getExtensions(type.java)
    override fun translate(key: String, bundleName: String, replacements: Array<Any?>): String =
        bot.translationProivder.translate(key, bundleName, replacements)
}

private fun ClassLoader.findTranslations(): Sequence<String> {
    val resourcePath = getResource("translations")?.file?.ensurePath()

    val path = resourcePath?.let { path -> Path(path) }
    if (path != null && path.isDirectory()) {
        return path.listDirectoryEntries()
            .asSequence()
            .filter { it.isDirectory() }
            .map { it.name }
    }

    return emptySequence()
}
