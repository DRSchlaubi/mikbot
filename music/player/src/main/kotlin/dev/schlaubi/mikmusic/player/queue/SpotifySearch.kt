package dev.schlaubi.mikmusic.player.queue

import com.neovisionaries.i18n.CountryCode
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.models.TrackResponse
import dev.schlaubi.mikmusic.core.Config
import dev.schlaubi.stdx.coroutines.parallelMapNotNullIndexed
import kotlinx.coroutines.future.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.apache.http.client.utils.URIBuilder
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.model_objects.IPlaylistItem
import se.michaelthelin.spotify.model_objects.specification.*
import se.michaelthelin.spotify.model_objects.specification.Playlist
import se.michaelthelin.spotify.requests.AbstractRequest
import kotlin.time.Duration.Companion.seconds
import se.michaelthelin.spotify.model_objects.specification.Track as SpotifyTrack

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
    tokenExpiry = Clock.System.now() + credentials.expiresIn.seconds
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
    val album = api().getAlbumsTracks(albumId).build().await()

    val tracks = album.items

    return tracks.mapToTracks(link) { it.toPartialSpotifyTrack() }
}

private suspend fun buildPlaylist(link: Link, matchResult: MatchResult): List<Track> {
    val (playlistId) = matchResult.destructured
    val playlist = getPlaylistItems(playlistId)

    val tracks = playlist.items ?: return emptyList()

    return tracks.mapToTracks(link, maxConcurrentRequests = 3) {
        it.track.toPartialSpotifyTrack()
    }
}

suspend fun getPlaylist(playlistId: String): Playlist? = api().getPlaylist(playlistId).build().await()
suspend fun getPlaylistItems(playlistId: String): Paging<PlaylistTrack> =
    api().getPlaylistsItems(playlistId).build().await()

private data class IndexedTrack(val index: Int, val track: Track)

private suspend fun <T> Array<T>.mapToTracks(
    link: Link,
    maxConcurrentRequests: Int? = null,
    mapper: suspend (T) -> PartialSpotifyTrack
): List<Track> = toList().parallelMapNotNullIndexed(maxConcurrentRequests) { index, item ->
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

    return track.toPartialSpotifyTrack().findTrack(link)
}

suspend fun PartialSpotifyTrack.findTrack(link: Link): Track? {
    val query = isrc ?: "$name${artists.firstOrNull()?.let { "- $it" } ?: ""}"
    return link.takeFirstMatch(query)
}

private suspend fun Link.takeFirstMatch(query: String): Track? {
    val result = loadItem("ytsearch: $query")
    return when (result.loadType) {
        TrackResponse.LoadType.SEARCH_RESULT -> result.tracks.first().toTrack()
        else -> null
    }
}

private suspend fun <T> AbstractRequest<T>.await(): T = executeAsync().await()

fun String.spotifyUriToUrl(): String {
    val (_, type, id) = split(":")

    return "https://open.spotify.com/$type/$id"
}

data class PartialSpotifyTrack(val artists: List<String>, val previewUrl: String?, val name: String, val isrc: String?)

fun IPlaylistItem.toPartialSpotifyTrack() = when (this) {
    is SpotifyTrack -> toPartialSpotifyTrack()
    is Episode -> PartialSpotifyTrack(emptyList(), audioPreviewUrl, name, null)
    else -> error("Unknown track type: ${this::class.qualifiedName}")
}

fun SpotifyTrack.toPartialSpotifyTrack() =
    PartialSpotifyTrack(artists.map { it.name }, previewUrl, name, externalIds.externalIds["isrc"])

fun TrackSimplified.toPartialSpotifyTrack() =
    PartialSpotifyTrack(artists.map { it.name }, previewUrl, name, null)
