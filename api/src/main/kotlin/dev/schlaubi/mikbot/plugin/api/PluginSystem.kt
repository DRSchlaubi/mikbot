package dev.schlaubi.mikbot.plugin.api

import org.pf4j.ExtensionPoint
import kotlin.reflect.KClass

/**
 * Internal [PluginSystem] instance.
 */
@InternalAPI
public lateinit var _pluginSystem: PluginSystem

/**
 * Global instance for [PluginSystem].
 */
@OptIn(InternalAPI::class)
public val pluginSystem: PluginSystem get() = _pluginSystem

/**
 * API for plugin related actions.
 *
 * @see pluginSystem
 */
public interface PluginSystem {
    /**
     * Retrieves all extensions of the [extension point][type].
     */
    public fun <T : ExtensionPoint> getExtensions(type: KClass<T>): List<T>

    /**
     * Translates [key] from [bundleName] with [replacements].
     */
    public fun translate(key: String, bundleName: String, replacements: Array<Any?>): String
}
/**
 * Retrieves all extensions of the [extension point][T].
 */
public inline fun <reified T : ExtensionPoint> PluginSystem.getExtensions(): List<T> = getExtensions(T::class)
