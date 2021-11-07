package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import org.pf4j.DefaultPluginManager
import org.pf4j.PluginManager
import kotlin.reflect.KClass

object PluginLoader : PluginManager by DefaultPluginManager() {
    internal val system: PluginSystem = DefaultPluginSystem(this)

    val botPlugins: List<Plugin> get() = plugins.map { it.plugin.asPlugin() }
}

private class DefaultPluginSystem(private val manager: PluginManager) : PluginSystem {
    override fun <T : Any> getExtensions(type: KClass<T>): List<T> = manager.getExtensions(type.java)
}
