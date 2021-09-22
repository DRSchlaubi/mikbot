package dev.schlaubi.musicbot.module.music.player

import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Track
import java.util.LinkedList

class MusicPlayer(private val link: Link) : Link by link {
    private val queue = LinkedList<Track>()
    val queuedTracks get() = queue.toList()

    init {
        link.player.on(consumer = ::onTrackEnd)
    }

    val nextSongIsFirst: Boolean get() = queue.isEmpty() && link.player.playingTrack == null

    suspend fun queueTrack(force: Boolean, onTop: Boolean, tracks: Collection<Track>) {
        val isFirst = nextSongIsFirst
        val startIndex = if ((onTop || force) && queue.isNotEmpty()) 1 else 0
        queue.addAll(startIndex, tracks)

        if (force || isFirst) {
            startNextSong()
        }
    }

    private suspend fun onTrackEnd(event: TrackEndEvent) {
        if (queue.isEmpty() && event.reason != TrackEndEvent.EndReason.REPLACED) link.disconnectAudio()
        if (event.reason.mayStartNext) {
            startNextSong()
        }
    }

    suspend fun skip() = startNextSong()

    private suspend fun startNextSong() {
        val nextTrack = queue.poll()
        link.player.playTrack(nextTrack)
    }
}
