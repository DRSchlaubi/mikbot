package dev.schlaubi.mikmusic.player

import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kordex.core.extensions.event
import dev.schlaubi.mikmusic.core.MusicModule
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList

suspend fun MusicModule.voiceStateWatcher() = event<VoiceStateUpdateEvent> {
    check {
        failIf {
            val guild = event.state.getGuild()
            val channelId = event.state.channelId
            val voiceStates = guild.voiceStates.filter { it.channelId == channelId }.toList()

            voiceStates.size > 1 || voiceStates.none { it.userId == kord.selfId }
        }
    }

    action {
        getMusicPlayer(event.state.getGuild()).stop() // Leave if the bot is the last user in VC
    }
}
