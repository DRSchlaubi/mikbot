package dev.schlaubi.musicbot.module.song_quiz.game

import com.wrapper.spotify.model_objects.specification.Playlist
import com.wrapper.spotify.model_objects.specification.Track
import dev.schlaubi.musicbot.module.music.player.queue.getTrack
import dev.schlaubi.musicbot.utils.parallelMapNotNull
import dev.schlaubi.uno.poll
import java.util.LinkedList

class TrackContainer private constructor(
    private val tracks: List<Track>,
    private val artistPool: LinkedList<String>,
    private val songNamePool: LinkedList<String>
) : List<Track> by tracks {

    fun pollArtistNames(blacklist: String, amount: Int = 3): List<String> =
        artistPool.poll(blacklist, amount)

    fun pollSongNames(blacklist: String, amount: Int = 3): List<String> =
        songNamePool.poll(blacklist, amount)

    private fun LinkedList<String>.poll(blacklist: String, amount: Int): List<String> {
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

            val artists = ArrayList<String>(playlistTracks.size)
            val names = ArrayList<String>(playlistTracks.size)
            playlistTracks.forEach {
                artists.add(it.artists.first().name)
                names.add(it.name)
            }
            val artistPool = LinkedList(artists)
            val songNamePool = LinkedList(names)

            val tracks = playlistTracks.take(size.coerceAtMost(playlistTracks.size))

            return TrackContainer(tracks, artistPool, songNamePool)
        }
    }
}
