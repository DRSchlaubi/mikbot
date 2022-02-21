package dev.schlaubi.mikbot.util_plugins.leaderboard

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.util_plugins.leaderboard.commands.importMee6
import dev.schlaubi.mikbot.util_plugins.leaderboard.commands.leaderBoardCommand
import dev.schlaubi.mikbot.util_plugins.leaderboard.core.LeaderBoardModule
import org.pf4j.Extension

@PluginMain
class LeaderBoardPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::LeaderBoardModule)
    }
}

@Extension
class LeaderBoardSettingsExtension : SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        leaderBoardCommand()
        importMee6()
    }
}
