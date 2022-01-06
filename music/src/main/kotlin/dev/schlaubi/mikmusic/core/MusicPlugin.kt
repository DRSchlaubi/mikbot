package dev.schlaubi.mikmusic.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.mikmusic.musicchannel.MusicInteractionModule
import dev.schlaubi.mikmusic.playlist.TrackListSerializer
import dev.schlaubi.mikmusic.playlist.commands.PlaylistModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.litote.kmongo.serialization.registerSerializer

@PluginMain
class MusicPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun start() {
        registerSerializer(TrackSerializer)
        registerSerializer(TrackListSerializer)
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::LavalinkManager)
        add(::MusicModule)
        add(::PlaylistModule)
        add(::MusicInteractionModule)
    }

    override fun CoroutineScope.atLaunch(bot: ExtensibleBot) {
        launch {
            bot.findExtension<LavalinkManager>()!!.load()
        }
    }
}
