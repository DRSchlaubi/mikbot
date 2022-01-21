package dev.schlaubi.mikbot.game.music_quiz.game

import com.wrapper.spotify.model_objects.specification.Track
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikmusic.player.queue.getArtist
import dev.schlaubi.mikmusic.player.queue.spotifyUriToUrl

suspend fun EmbedBuilder.addTrack(track: Track) {
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
    }

    field {
        name = "Album"
        value = track.album.name
    }

    field {
        name = "Artists"
        value = track.artists.joinToString(", ") { it.name }
    }

    footer {
        text = "Next song starts in 3 seconds"
    }
}
