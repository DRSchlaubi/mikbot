package dev.schlaubi.mikmusic.util

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.nycode.imagecolor.ImageColorClient
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.mikbot.plugin.api.util.Translator

private val imageColorClient by KordExContext.get().inject<ImageColorClient>()

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

        imageColorClient.fetchImageColorOrNull(info.thumbnails.high.url)?.let { imageColor ->
            color = Color(imageColor)
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
