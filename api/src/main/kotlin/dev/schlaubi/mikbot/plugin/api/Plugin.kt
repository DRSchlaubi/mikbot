package dev.schlaubi.mikbot.plugin.api

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import kotlinx.coroutines.CoroutineScope
import org.pf4j.Plugin as PF4JPlugin
import org.pf4j.PluginWrapper as PF4JPluginWrapper

/**
 * Plugin wrapper alias.
 */
public typealias PluginWrapper = PF4JPluginWrapper

/**
 * Main class of a plugin.
 *
 * @see PluginMain
 */
public abstract class Plugin(wrapper: PluginWrapper) : PF4JPlugin(wrapper), KordExKoinComponent {
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
