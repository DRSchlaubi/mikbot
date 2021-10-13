package dev.schlaubi.musicbot.utils

import dev.schlaubi.musicbot.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val happiUrl = Url("https://api.happi.dev/v1")

private val client = HttpClient {
    install(JsonFeature) {
        val json = Json {
            ignoreUnknownKeys = true
        }

        serializer = KotlinxSerializer(json)
    }

    defaultRequest {
        header("x-happi-key", Config.HAPPI_KEY)
    }
}

/**
 * Finds [limit] songs matching [name].
 *
 * @param artist limit the search results by the artist name
 * @param onlyLyrics only find songs with lyrics
 *
 * @see HappiResult
 * @see HappiTrack
 */
suspend fun searchHappiSong(
    name: String,
    limit: Int? = 1,
    onlyLyrics: Boolean = true
): HappiResult<List<HappiTrack>> = client.get(
    happiUrl
) {
    url {
        path("v1", "music")
    }

    parameter("q", name)
    parameter("limit", limit)
    parameter("lyrics", onlyLyrics)
}

/**
 * Finds the [HappiLyrics] by the specified identifiers.
 *
 * @see HappiResult
 * @see HappiLyrics
 */
suspend fun fetchHappiLyrics(
    artistId: Int,
    albumId: Int,
    trackId: Int
): HappiResult<HappiLyrics> = client.get(happiUrl) {
    url {
        path(
            "v1", "music", "artists",
            artistId.toString(),
            "albums",
            albumId.toString(),
            "tracks",
            trackId.toString(),
            "lyrics"
        )
    }
}

/**
 * Finds the [HappiLyrics] for this [HappiTrack].
 *
 * @see fetchHappiLyrics
 */
suspend fun HappiTrack.fetchLyrics() = fetchHappiLyrics(artistId, albumId, trackId)

/**
 * Result for Happi api requests.
 *
 * @property success whether this request succeeded or not
 * @property length the amount of [result] items
 * @property result the result
 * @property error the error if an error happened
 */
@JvmRecord
@Serializable
data class HappiResult<T>(
    val success: Boolean,
    val length: Int? = null,
    val result: T? = null,
    val error: String? = null
)

@Serializable
@JvmRecord
data class HappiTrack(
    val track: String,
    @SerialName("id_track")
    val trackId: Int,
    val artist: String,
    @SerialName("id_artist")
    val artistId: Int,
    val album: String,
    @SerialName("id_album")
    val albumId: Int
)

@Serializable
@JvmRecord
data class HappiLyrics(
    val lyrics: String,
    @SerialName("copyright_label")
    val copyrightLabel: String,
    @SerialName("copyright_notice")
    val copyrightNotice: String
)
