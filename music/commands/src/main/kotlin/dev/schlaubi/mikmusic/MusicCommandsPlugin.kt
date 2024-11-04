package dev.schlaubi.mikmusic

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikmusic.commands.commands
import dev.schlaubi.mikmusic.context.playMessageAction
import dev.schlaubi.mikmusic.core.MusicExtensionPoint
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.playlist.commands.PlaylistModule
import org.pf4j.Extension

@PluginMain
class MusicCommandsPlugin(context: PluginContext) : Plugin(context) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::PlaylistModule)
    }
}

@Extension
class MusicCommandsExtension : MusicExtensionPoint {
    override suspend fun MusicModule.overrideSetup() {
        commands()
        playMessageAction()
    }
}
