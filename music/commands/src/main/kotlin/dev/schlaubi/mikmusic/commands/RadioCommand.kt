package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string

class RadioArguments : Arguments() {
    val query by string {
        name = "query"
        description = "commands.radio.arguments.query.description"
    }
}

/*
suspend fun MusicModule.radioCommand() {
    ephemeralSlashCommand(::RadioArguments) {
        name = "radio"
        description = "commands.radio.description"

        check {
            joinSameChannelCheck(bot)
        }

        action {
            val results = musicPlayer.loadItem("ytsearch: ${arguments.query}")
            val track = results.tracks.firstOrNull()?.toTrack()
            val videoId = track?.youtubeId
            if (track == null || videoId == null) {
                discordError(translate("commands.radio.invalid_vide"))
            }

            musicPlayer.enableAutoPlay(videoId, radioParam)
            musicPlayer.queueTrack(force = false, onTop = false, tracks = listOf(SimpleQueuedTrack(track, user.id)))

            respond {
                content = translate("commands.radio.queued")
            }
        }
    }
}
*/
