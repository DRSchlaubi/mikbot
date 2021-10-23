package dev.schlaubi.musicbot.module.music.player.queue

import com.neovisionaries.i18n.CountryCode
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.Artist
import com.wrapper.spotify.model_objects.specification.Playlist
import com.wrapper.spotify.model_objects.specification.TrackSimplified
import com.wrapper.spotify.requests.AbstractRequest
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.utils.parallelMapNotNull
import dev.schlaubi.musicbot.utils.parallelMapNotNullIndexed
import io.ktor.utils.io.preventFreeze
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.apache.http.client.utils.URIBuilder
import kotlin.time.Duration
import com.wrapper.spotify.model_objects.specification.Track as SpotifyTrack

val PLAYLIST_PATTERN =
    "https?://.*\\.spotify\\.com/playlists?/([^?/\\s]*)".toRegex()
private val TRACK_PATTERN = "https?://.*\\.spotify\\.com/tracks?/([^?/\\s]*)".toRegex()
private val ALBUM_PATTERN = "https?://.*\\.spotify\\.com/albums?/([^?/\\s]*)".toRegex()

private var tokenExpiry: Instant? = null

private val api = SpotifyApi.Builder()
    .setClientId(Config.SPOTIFY_CLIENT_ID)
    .setClientSecret(Config.SPOTIFY_CLIENT_SECRET)
    .build()

private suspend fun api(): SpotifyApi {
    val expiry = tokenExpiry
    if (expiry != null && Clock.System.now() < expiry) {
        return api
    }

    val credentials = api.clientCredentials().build().await()
    tokenExpiry = Clock.System.now() + Duration.seconds(credentials.expiresIn)
    return api.apply {
        accessToken = credentials.accessToken
    }
}

suspend fun findSpotifySongs(link: Link, query: String): List<Track>? {
    val url = URIBuilder(query).removeQuery().toString()

    val trackMatch = TRACK_PATTERN.find(url)
    val albumMatch by lazy { ALBUM_PATTERN.find(url) }
    val playlistMatch by lazy { PLAYLIST_PATTERN.find(url) }
    return when {
        trackMatch != null -> buildTrack(link, trackMatch)
        albumMatch != null -> buildAlbum(link, albumMatch!!)
        playlistMatch != null -> buildPlaylist(link, playlistMatch!!)
        else -> return null
    }
}

private suspend fun buildAlbum(link: Link, matchResult: MatchResult): List<Track> {
    val (albumId) = matchResult.destructured
    val album = api().getAlbum(albumId).build().await()

    val tracks = album.tracks.items

    return tracks.mapToTracks(link) { it.toNamedTrack() }
}

private suspend fun buildPlaylist(link: Link, matchResult: MatchResult): List<Track> {
    val (playlistId) = matchResult.destructured
    val playlist = getPlaylist(playlistId)

    val tracks = playlist?.tracks?.items ?: return emptyList()

    return tracks.mapToTracks(link, maxConcurrentRequests = 3) {
        it.track.id?.let { id ->
            val track = api().getTrack(id).build().await()

            track.toNamedTrack()
        } ?: NamedTrack(it.track.name, null)
    }
}

suspend fun getPlaylist(playlistId: String): Playlist? = api().getPlaylist(playlistId).build().await()

@JvmRecord
private data class IndexedTrack(val index: Int, val track: Track)

private suspend fun <T> Array<T>.mapToTracks(
    link: Link,
    maxConcurrentRequests: Int? = null,
    mapper: suspend (T) -> NamedTrack
): List<Track> = toList().parallelMapNotNullIndexed { index, item ->
    val found = mapper(item).findTrack(link)
    found?.let { IndexedTrack(index, it) }
}
    .asSequence()
    .sortedBy { it.index }
    .map { it.track }
    .toList()

private suspend fun buildTrack(link: Link, trackMatch: MatchResult): List<Track> {
    val (trackId) = trackMatch.destructured

    return listOfNotNull(getTrack(link, trackId))
}

suspend fun getTrack(trackId: String): SpotifyTrack = api().getTrack(trackId)
    .market(CountryCode.US).build().await()

suspend fun getArtist(artistId: String): Artist = api().getArtist(artistId).build().await()

private suspend fun getTrack(link: Link, trackId: String): Track? {
    val track = getTrack(trackId)

    return track.toNamedTrack().findTrack(link)
}

suspend fun NamedTrack.findTrack(link: Link): Track? =
    link.takeFirstMatch("$name${artist?.let { "- $it" } ?: ""}")

private suspend fun Link.takeFirstMatch(query: String): Track? {
    val result = loadItem("ytsearch: $query")
    return when (result.loadType) {
        TrackResponse.LoadType.SEARCH_RESULT -> result.tracks.first().toTrack()
        else -> null
    }
}

fun SpotifyTrack.toNamedTrack(): NamedTrack = NamedTrack(name, artists.first().name)
private fun TrackSimplified.toNamedTrack(): NamedTrack = NamedTrack(name, artists.first().name)

@JvmRecord
data class NamedTrack(val name: String, val artist: String?)

private suspend fun <T> AbstractRequest<T>.await(): T = executeAsync().await()

fun String.spotifyUriToUrl(): String {
    val (_, type, id) = split(":")

    return "https://open.spotify.com/$type/$id"
}
