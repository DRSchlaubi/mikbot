package dev.schlaubi.mikbot.plugin.api

import kotlin.reflect.KClass

public val pluginSystem: PluginSystem = TODO()

public interface PluginSystem {
    public fun <T : Any> getExtensions(type: KClass<T>): List<T>
}

public inline fun <reified T : Any> PluginSystem.getExtensions(): List<T> = getExtensions(T::class)
