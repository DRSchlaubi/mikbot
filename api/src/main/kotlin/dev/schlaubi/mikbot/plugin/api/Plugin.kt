package dev.schlaubi.mikbot.plugin.api

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.mikbot.plugin.api.io.Database
import kotlinx.coroutines.CoroutineScope
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.util.*
import org.pf4j.PluginWrapper as PF4JPluginWrapper

/**
 * Plugin wrapper alias.
 */
@Deprecated("Replaced by PluginContext", ReplaceWith("PluginContext", "dev.schlaubi.mikbot.plugin.api.PluginContext"))
public typealias PluginWrapper = PF4JPluginWrapper

public interface PluginContext {
    public val pluginSystem: PluginSystem
    public val database: Database
    public val pluginWrapper: PF4JPluginWrapper
}


public abstract class Plugin : Plugin {
    /**
     * Getter for [PluginContext].
     *
     * @return the context or `null` if it is a legacy plugin
     * @see isLegacyPlugin
     */
    public val context: PluginContext?

    /**
     * Fall back constructor
     *
     * @param wrapper the [PluginWrapper] provided by the plugin engine
     */
    @Deprecated("Deprecated by PF4J (Use Plugin#Plugin(PluginContext) instead")
    public constructor(wrapper: PF4JPluginWrapper) : super(wrapper) {
        context = null
    }

    /**
     * Constructor of a plugin.
     *
     * @param context the [PluginContext] provided by the engine
     */
    public constructor(context: PluginContext) : super() {
        Objects.requireNonNull(context, "Context needs not to be null")
        this.context = context
    }

    /**
     * Checks whether the plugin is running in legacy mode.
     *
     * @return whether the plugin is running in legacy mode or not
     */
    public val isLegacyPlugin: Boolean
        get() = context == null

    /**
     * Getter for [PluginContext].
     *
     * @return the context or `null`
     * @see isLegacyPlugin
     * @throws IllegalStateException if this is a legacy plugin
     */
    public val contextSafe: PluginContext
        get() {
            checkNotNull(context) { "This plugin is a legacy plugin" }
            return context
        }

    /**
     * Add additional [ExtensibleBot] settings.
     *
     * **Do not add [Extensions][Extension] in here, use [addExtensions] instead.**
     */
    public open suspend fun ExtensibleBotBuilder.apply(): Unit = Unit

    /**
     * Add new extensions.
     */
    public open fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions(): Unit = Unit

    /**
     * This is being executed directly after the bot got started.
     */
    public open fun CoroutineScope.atLaunch(bot: ExtensibleBot): Unit = Unit
}
