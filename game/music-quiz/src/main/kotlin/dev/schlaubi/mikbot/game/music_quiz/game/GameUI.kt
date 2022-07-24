package dev.schlaubi.mikbot.game.music_quiz.game

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikmusic.player.queue.getArtist
import dev.schlaubi.mikmusic.player.queue.spotifyUriToUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import se.michaelthelin.spotify.model_objects.specification.Track

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
        color = fetchAlbumColor(thumbnailUrl)
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

private val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

private suspend fun fetchAlbumColor(url: String): Color = withContext(Dispatchers.IO) {
    val imageResponse = httpClient.get(url)
    val image = imageResponse.bodyAsChannel()
    val imageContentType = imageResponse.contentType() ?: ContentType.Image.JPEG
    val (colors) = httpClient.post(URLBuilder(SongQuizConfig.IMAGE_COLOR_SERVICE_URL).apply {
        appendPathSegments("color")
    }.build()) {
        contentType(imageContentType)
        setBody(image)
    }.body<ColorResponse>()
    colors.first()
}

@Serializable
data class ColorResponse(val colors: List<Color>)

suspend fun main() {
    println(fetchAlbumColor("https://i.scdn.co/image/ab67616d00001e02f0f3a191d7dcaf2b6a8ac86c"))
}
