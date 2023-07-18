package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.innerttube.radioParam
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack
import dev.schlaubi.mikmusic.player.enableAutoPlay
import dev.schlaubi.mikmusic.util.youtubeId

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
