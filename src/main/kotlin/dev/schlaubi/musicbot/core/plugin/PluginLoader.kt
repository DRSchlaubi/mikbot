package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import mu.KotlinLogging
import org.pf4j.*
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.KClass

private val LOG = KotlinLogging.logger { }

object PluginLoader : DefaultPluginManager() {
    internal val system: PluginSystem = DefaultPluginSystem(this)
    private val rootTranslations = ClassLoader.getSystemClassLoader().findTranslations()
    private lateinit var pluginBundles: Map<String, String>

    override fun getPluginDescriptorFinder(): PluginDescriptorFinder = MikBotPluginDescriptionFinder()

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadPlugins() {
        super.loadPlugins()

        pluginBundles = buildMap {
            plugins.values.forEach { plugin ->
                val resourcePath = plugin.pluginClassLoader.getResource("translations")?.file

                if (resourcePath != null) {
                    val path = Path(resourcePath)
                    path.listDirectoryEntries()
                        .asSequence()
                        .filter { it.isDirectory() }
                        .map { it.name }
                        .filterNot { it in rootTranslations }
                        .forEach {
                            this[it] = plugin.pluginId
                        }
                }
            }
        }

        LOG.debug { "Built translation provider graph: $pluginBundles" }
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
