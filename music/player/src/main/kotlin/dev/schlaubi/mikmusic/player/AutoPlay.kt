package dev.schlaubi.mikmusic.player

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.Translator
import dev.schlaubi.mikmusic.util.TrackLinkedListSerializer
import dev.schlaubi.mikmusic.util.TrackListSerializer
import dev.schlaubi.mikmusic.util.format
import dev.schlaubi.mikmusic.util.spotifyId
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AutoPlayContext(
    val seedGenres: List<String>,
    val seedArtists: List<String>,
    @Serializable(with = TrackListSerializer::class)
    val history: List<Track>,
    @Serializable(with = TrackLinkedListSerializer::class) val songs: LinkedList<Track> = LinkedList(),
) {
    val initialSize = songs.size

    companion object {
        val EMPTY = AutoPlayContext(emptyList(), emptyList(), emptyList())
    }
}


suspend fun MusicPlayer.resetAutoPlay() {
    val state = autoPlay ?: return
    autoPlay = null
    if (state.songs.size != state.initialSize) {
        skip()
    }
    updateMusicChannelMessage()
}

suspend fun MusicPlayer.enableAutoPlay() {
    val seedTracks = queue.tracks.mapNotNull { it.track.spotifyId }
    if (seedTracks.isEmpty()) error("Spotify tracks are required")
    fetchAutoPlay(seedTracks = seedTracks)
}

suspend fun MusicPlayer.enableAutoPlay(
    seedTracks: List<String> = emptyList(),
    seedArtists: List<String> = emptyList(),
    seedGenres: List<String> = emptyList(),
) {
    autoPlay = AutoPlayContext(seedGenres, seedArtists, emptyList())
    fetchAutoPlay(seedTracks = seedTracks)
}

private suspend fun MusicPlayer.fetchAutoPlay(
    seedTracks: List<String> = emptyList(),
    seedArtists: List<String> = emptyList(),
    seedGenres: List<String> = emptyList(),
) {
    val query = buildString {
        append("sprec:")
        if (seedTracks.isNotEmpty()) {
            append("seed_tracks=${seedTracks.take(5).joinToString(",")}")
        }
        if (seedArtists.isNotEmpty()) {
            append("seed_artists=${seedTracks.take((5 - seedTracks.size).coerceAtLeast(0)).joinToString(",")}")
        }
        if (seedGenres.isNotEmpty()) {
            append(
                "seed_genres=${
                    seedTracks.take((5 - seedTracks.size - seedArtists.size).coerceAtLeast(0)).joinToString(",")
                }"
            )
        }
    }
    val (_, list) = link.loadItem(query) as LoadResult.PlaylistLoaded
    autoPlay = (autoPlay ?: AutoPlayContext.EMPTY).copy(history = list.tracks, songs = LinkedList(list.tracks))
}

context(EmbedBuilder)
suspend fun MusicPlayer.addAutoPlaySongs(translate: Translator) {
    val songs = autoPlay?.songs?.take(5)
    if (!songs.isNullOrEmpty()) {
        field {
            name = translate("music.auto_play.next_song", "music")
            value = songs.joinToString<Track>("\n", transform = Track::format)
        }
    }
}

suspend fun MusicPlayer.findNextAutoPlayedSong(): Track? {
    val currentAutoPlay = autoPlay ?: return null
    if (currentAutoPlay.songs.isNotEmpty()) {
        return currentAutoPlay.songs.poll()
    }
    fetchAutoPlay(currentAutoPlay.history.mapNotNull(Track::spotifyId))
    return autoPlay?.songs?.poll()
}

