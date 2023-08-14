package dev.schlaubi.mikmusic.util

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.nycode.imagecolor.ImageColorClient
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.mikbot.plugin.api.util.Translator
import dev.schlaubi.mikmusic.core.Config
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
        value = "[${track.info.title}](${track.info.uri})"
    }

    field {
        name = translate("music.track.duration", "music")
        value = track.info.length.toDuration(DurationUnit.MILLISECONDS).toString()
    }

    val thumbnailUrl = track.info.artworkUrl
    if (thumbnailUrl != null) {
        thumbnail {
            url = thumbnailUrl
        }

        if (Config.IMAGE_COLOR_SERVICE_URL != null) {
            imageColorClient.fetchImageColorOrNull(thumbnailUrl)?.let { imageColor ->
                color = Color(imageColor)
            }
        }
    }

    val video = track.findOnYoutube()
    val lavaSrcInfo = runCatching { track.lavaSrcInfo }.getOrNull()
    if (video != null) {
        val info = video.snippet
        val channel = getFirstChannelById(info.channelId).snippet

        author {
            name = channel.title
            url = "https://www.youtube.com/channel/${info.channelId}"
            icon = channel.thumbnails.high.url
        }
    } else {
        author {
            name = track.info.author
            if (lavaSrcInfo != null) {
                if (lavaSrcInfo.artistUrl != null) {
                    url = lavaSrcInfo.artistUrl
                }
                if(lavaSrcInfo.artistArtworkUrl != null) {
                    icon = lavaSrcInfo.artistArtworkUrl
                }
            }
        }

        if (lavaSrcInfo?.albumName != null) {
            field {
                name = translate("music.track.album", "music")
                value = if (lavaSrcInfo.albumUrl != null) {
                    "[${lavaSrcInfo.albumName}](${lavaSrcInfo.albumUrl})"
                } else {
                    lavaSrcInfo.albumName!!
                }
            }
        }
    }
}
