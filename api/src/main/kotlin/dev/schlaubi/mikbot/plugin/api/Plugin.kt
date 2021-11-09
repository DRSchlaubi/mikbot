package dev.schlaubi.mikbot.plugin.api

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import kotlinx.coroutines.CoroutineScope
import org.pf4j.Plugin as PF4JPlugin
import org.pf4j.PluginWrapper as PF4JPluginWrapper

public typealias PluginWrapper = PF4JPluginWrapper

public abstract class Plugin(wrapper: PluginWrapper) : PF4JPlugin(wrapper) {
    public open suspend fun ExtensibleBotBuilder.apply(): Unit = Unit

    public open fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions(): Unit = Unit

    public open fun CoroutineScope.atLaunch(bot: ExtensibleBot): Unit = Unit
}
