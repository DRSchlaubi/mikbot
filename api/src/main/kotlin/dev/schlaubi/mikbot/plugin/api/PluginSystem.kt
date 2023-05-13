package dev.schlaubi.mikbot.plugin.api

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import org.pf4j.ExtensionPoint
import kotlin.reflect.KClass

/**
 * Internal [PluginSystem] instance.
 */
@InternalAPI
public lateinit var _pluginSystem: PluginSystem

/**
 * Global instance for [PluginSystem].
 *
 * @see MikBotModule
 */
@OptIn(InternalAPI::class)
@Deprecated("Replaced by PluginContext", ReplaceWith("PluginContext.pluginSystem"))
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
    public fun translate(
        key: String,
        bundleName: String,
        locale: String? = null,
        replacements: Array<Any?> = emptyArray(),
    ): String

    /**
     * Emits [event] on [Kord.events].
     */
    public suspend fun emitEvent(event: Event)
}

/**
 * Retrieves all extensions of the [extension point][T].
 */
public inline fun <reified T : ExtensionPoint> PluginSystem.getExtensions(): List<T> = getExtensions(T::class)
