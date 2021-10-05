package dev.schlaubi.musicbot.module.song_quiz.game

import com.wrapper.spotify.model_objects.specification.Playlist
import com.wrapper.spotify.model_objects.specification.Track
import dev.schlaubi.musicbot.module.music.player.queue.getTrack
import dev.schlaubi.musicbot.utils.parallelMapNotNull
import dev.schlaubi.uno.poll
import java.util.LinkedList

class TrackContainer private constructor(
    val spotifyPlaylist: Playlist,
    private val tracks: List<Track>,
    private val artistPool: LinkedList<String>,
    private val songNamePool: LinkedList<String>
) : List<Track> by tracks {

    private val allArtists = ArrayList(artistPool)
    private val allSongs = ArrayList(songNamePool)

    fun pollArtistNames(blacklist: String, amount: Int = 3): List<String> =
        artistPool.poll(allArtists, blacklist, amount)

    fun pollSongNames(blacklist: String, amount: Int = 3): List<String> =
        songNamePool.poll(allSongs, blacklist, amount)

    private fun LinkedList<String>.poll(backupPool: List<String>, blacklist: String, amount: Int): List<String> {
        if (size <= amount) {
            addAll(backupPool)
        }
        val allowed = LinkedList(this - blacklist)
        val options = allowed.poll(amount)
        removeAll(options + blacklist)

        return options
    }

    companion object {
        suspend operator fun invoke(playlist: Playlist, size: Int): TrackContainer {
            val playlistTracks = playlist.tracks.items.toList().shuffled().parallelMapNotNull {
                it.track.id?.let { id -> getTrack(id) }
            }

            val artists = HashSet<String>(playlistTracks.size)
            val names = HashSet<String>(playlistTracks.size)
            playlistTracks.forEach {
                artists.add(it.artists.first().name)
                names.add(it.name)
            }
            val artistPool = LinkedList(artists)
            val songNamePool = LinkedList(names)

            val tracks = playlistTracks.take(size.coerceAtMost(playlistTracks.size))

            return TrackContainer(playlist, tracks, artistPool, songNamePool)
        }
    }
}
