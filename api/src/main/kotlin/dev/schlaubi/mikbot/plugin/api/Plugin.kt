package dev.schlaubi.mikbot.plugin.api

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.schlaubi.mikbot.plugin.api.io.Database
import kotlinx.coroutines.CoroutineScope
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

/**
 * Basic plugin methods.
 *
 * **This is only used because of Java compatibility, please only extend [Plugin]**
 */
public interface PluginInterface {
    /**
     * Add additional [ExtensibleBot] settings.
     *
     * **Do not add [Extensions][Extension] in here, use [addExtensions] instead.**
     */
    public suspend fun ExtensibleBotBuilder.apply(): Unit = Unit

    /**
     * Add new extensions.
     */
    public fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions(): Unit = Unit

    /**
     * This is being executed directly after the bot got started.
     */
    public fun CoroutineScope.atLaunch(bot: ExtensibleBot): Unit = Unit
}
