package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.player.Track

/**
 * This function fetches all required information for [track] and adds it to `this` [EmbedBuilder].
 *
 * @param commandContext [CommandContext] which supplies the translate function
 */
suspend fun EmbedBuilder.addSong(commandContext: CommandContext, track: Track) =
    addSong(commandContext::translate, track)

/**
 * This function fetches all required information for [track] and adds it to `this` [EmbedBuilder].
 *
 * @param translate A [Translator] to translate messages.
 */
suspend fun EmbedBuilder.addSong(translate: Translator, track: Track) {
    field {
        name = translate("music.track.title", "music")
        value = track.title
    }

    field {
        name = translate("music.track.duration", "music")
        value = track.length.toString()
    }

    val video = track.findOnYoutube()
    if (video != null) {
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
            name = translate("music.track.author", "music")
            value = track.author
        }
    }
}
