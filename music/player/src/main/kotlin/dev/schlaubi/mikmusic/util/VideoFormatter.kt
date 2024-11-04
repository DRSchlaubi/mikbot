package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.koin.KordExContext
import dev.kordex.core.types.TranslatableContext
import dev.nycode.imagecolor.ImageColorClient
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.mikbot.plugin.api.util.Translator
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.Config
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val imageColorClient by KordExContext.get().inject<ImageColorClient>()

/**
 * This function fetches all required information for [track] and adds it to `this` [EmbedBuilder].
 *
 * @param translator A [TranslatableContext] to translate messages.
 */
suspend fun EmbedBuilder.addSong(translator: TranslatableContext, track: Track) {
    field {
        name = translator.translate(MusicTranslations.Music.Track.title)
        value = "[${track.info.title}](${track.info.uri})"
    }

    field {
        name = translator.translate(MusicTranslations.Music.Track.duration)
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
                if (lavaSrcInfo.artistArtworkUrl != null) {
                    icon = lavaSrcInfo.artistArtworkUrl
                }
            }
        }

        if (lavaSrcInfo?.albumName != null) {
            field {
                name = translator.translate(MusicTranslations.Music.Track.album)
                value = if (lavaSrcInfo.albumUrl != null) {
                    "[${lavaSrcInfo.albumName}](${lavaSrcInfo.albumUrl})"
                } else {
                    lavaSrcInfo.albumName!!
                }
            }
        }
    }
}
