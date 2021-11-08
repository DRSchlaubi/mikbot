package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import mu.KotlinLogging
import org.pf4j.*
import org.pf4j.update.UpdateManager
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.KClass

private val LOG = KotlinLogging.logger { }

object PluginLoader : DefaultPluginManager() {
    internal val system: PluginSystem = DefaultPluginSystem(this)
    private val updateManager = UpdateManager(this)
    private val rootTranslations = ClassLoader.getSystemClassLoader().findTranslations()
    private lateinit var pluginBundles: Map<String, String>

    override fun getPluginDescriptorFinder(): PluginDescriptorFinder = MikBotPluginDescriptionFinder()

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadPlugins() {
        super.loadPlugins()

        //checkForUpdates()

        pluginBundles = buildMap {
            plugins.values.forEach { plugin ->
                plugin.pluginClassLoader.findTranslations().forEach {
                    this[it] = plugin.pluginId
                }
            }
        }

        LOG.debug { "Built translation provider graph: $pluginBundles" }
    }

    private fun checkForUpdates() {
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
    override fun <T : Any> getExtensions(type: KClass<T>): List<T> = manager.getExtensions(type.java)
}

private fun ClassLoader.findTranslations(): Sequence<String> {
    val resourcePath = getResource("translations")?.file

    if (resourcePath != null) {
        val path = Path(resourcePath)
        return path.listDirectoryEntries()
            .asSequence()
            .filter { it.isDirectory() }
            .map { it.name }
    }

    return emptySequence()
}
