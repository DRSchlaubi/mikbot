package dev.schlaubi.musicbot.module.music.player.queue

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.PlaylistTrack
import com.wrapper.spotify.model_objects.specification.TrackSimplified
import com.wrapper.spotify.requests.AbstractRequest
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.musicbot.config.Config
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.apache.http.client.utils.URIBuilder
import com.wrapper.spotify.model_objects.specification.Track as SpotifyTrack

private val PLAYLIST_PATTERN =
    "https?://.*\\.spotify\\.com/playlists?/([^?/\\s]*)".toRegex()
private val TRACK_PATTERN = "https?://.*\\.spotify\\.com/tracks?/([^?/\\s]*)".toRegex()
private val ALBUM_PATTERN = "https?://.*\\.spotify\\.com/albums?/([^?/\\s]*)".toRegex()

private val api = SpotifyApi.Builder()
    .setClientId(Config.SPOTIFY_CLIENT_ID)
    .setClientSecret(Config.SPOTIFY_CLIENT_SECRET)
    .build()

suspend fun initializeSpotifyApi() {
    val token = api.clientCredentials().build().await()

    api.accessToken = token.accessToken
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
    val album = api.getAlbum(albumId).build().await()

    val tracks = album.tracks.items

    return tracks.mapToTracks(link) { it.toNamedTrack() }
}

private suspend fun buildPlaylist(link: Link, matchResult: MatchResult): List<Track> {
    val (playlistId) = matchResult.destructured
    val playlist = api.getPlaylist(playlistId).build().await()

    val tracks = playlist.tracks.items

    return tracks.mapToTracks(link) { it.toNamedTrack() }
}

private suspend fun <T> Array<T>.mapToTracks(link: Link, mapper: suspend (T) -> NamedTrack): List<Track> {
    val list = ArrayList<Track>(size)

    coroutineScope {
        forEach {
            launch {
                val found = mapper(it).findTrack(link)
                if (found != null) {
                    list.add(found)
                }
            }
        }
    }

    return list
}

private suspend fun buildTrack(link: Link, trackMatch: MatchResult): List<Track> {
    val (trackId) = trackMatch.destructured

    return listOfNotNull(getTrack(link, trackId))
}

private suspend fun getTrack(link: Link, trackId: String): Track? {
    val track = api.getTrack(trackId).build().await()

    return track.toNamedTrack().findTrack(link)
}

private suspend fun NamedTrack.findTrack(link: Link): Track? =
    link.takeFirstMatch("$name${artist?.let { "- $it" } ?: ""}")

private suspend fun Link.takeFirstMatch(query: String): Track? {
    val result = loadItem("ytsearch: $query")
    return when (result.loadType) {
        TrackResponse.LoadType.SEARCH_RESULT -> result.tracks.first().toTrack()
        else -> null
    }
}

private fun SpotifyTrack.toNamedTrack(): NamedTrack = NamedTrack(name, artists.first().name)
private fun TrackSimplified.toNamedTrack(): NamedTrack = NamedTrack(name, artists.first().name)
private fun PlaylistTrack.toNamedTrack(): NamedTrack = NamedTrack(track.name, null)

@JvmRecord
private data class NamedTrack(val name: String, val artist: String?)

private suspend fun <T> AbstractRequest<T>.await(): T = executeAsync().await()
