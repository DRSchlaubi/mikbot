package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import org.pf4j.DefaultPluginManager
import org.pf4j.ManifestPluginDescriptorFinder
import org.pf4j.PluginDescriptorFinder
import org.pf4j.PluginManager
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.reflect.KClass

object PluginLoader : DefaultPluginManager() {
    internal val system: PluginSystem = DefaultPluginSystem(this)

    override fun getPluginDescriptorFinder(): PluginDescriptorFinder = MikBotPluginDescriptionFinder()

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
