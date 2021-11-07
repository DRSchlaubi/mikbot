package dev.schlaubi.mikbot.plugin.api

import kotlin.reflect.KClass

@InternalAPI
public lateinit var _pluginSystem: PluginSystem

@OptIn(InternalAPI::class)
public val pluginSystem: PluginSystem get() = _pluginSystem

public interface PluginSystem {
    public fun <T : Any> getExtensions(type: KClass<T>): List<T>
}

public inline fun <reified T : Any> PluginSystem.getExtensions(): List<T> = getExtensions(T::class)
