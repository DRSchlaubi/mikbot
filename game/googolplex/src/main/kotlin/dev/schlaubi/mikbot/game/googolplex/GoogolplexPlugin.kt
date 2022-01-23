package dev.schlaubi.mikbot.game.googolplex

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain
class GoogolplexPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::GoogolplexModule)
    }
}
