package dev.schlaubi.mikbot.game.music_quiz.game

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.nycode.imagecolor.ImageColorClient
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikmusic.player.queue.getArtist
import dev.schlaubi.mikmusic.player.queue.spotifyUriToUrl
import se.michaelthelin.spotify.model_objects.specification.Track

private val imageColorClient by KordExContext.get().inject<ImageColorClient>()

suspend fun EmbedBuilder.addTrack(track: Track, game: SongQuizGame) {
    author {
        val artist = track.artists.first()
        val spotifyArtist = getArtist(artist.id)

        name = track.name
        icon = spotifyArtist.images.firstOrNull()?.url
        url = track.uri.spotifyUriToUrl()
    }

    track.album.images.firstOrNull()?.url?.let { thumbnailUrl ->
        thumbnail {
            url = thumbnailUrl
        }
        imageColorClient.fetchImageColorOrNull(thumbnailUrl)?.let { imageColor ->
            color = Color(imageColor)
        }
    }

    field {
        name = game.translate("game.ui.album")
        value = track.album.name
    }

    field {
        name = game.translate("game.ui.artists")
        value = track.artists.joinToString(", ") { it.name }
    }

    footer {
        text = game.translate("game.ui.footer")
    }
}
