package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.player.Track

/**
 * This function fetches all required information for [track] and adds it to `this` [EmbedBuilder].
 *
 * @param commandContext [CommandContext] which supplies the translate function
 */
suspend fun EmbedBuilder.addSong(commandContext: CommandContext, track: Track) {
    field {
        name = commandContext.translate("music.track.title")
        value = track.title
    }

    field {
        name = commandContext.translate("music.track.duration")
        value = track.length.toString()
    }

    if (track.uri?.contains("youtu(?:be)?".toRegex()) == true) {
        val video = getVideoById(track.identifier)
        val info = video.snippet
        val channel = getFirstChannelById(info.channelId).snippet
        thumbnail {
            url = info.thumbnails.high.url
        }

        author {
            name = channel.title
            url = "https://www.youtube.com/channel/${info.channelId}"
            icon = channel.thumbnails.high.url
        }
    } else {
        field {
            name = commandContext.translate("music.track.author")
            value = track.author
        }
    }
}
