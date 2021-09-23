package dev.schlaubi.musicbot.module.music.player

import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Track
import java.util.LinkedList
import kotlin.random.Random

class MusicPlayer(private val link: Link) : Link by link {
    private val queue = LinkedList<Track>()
    val queuedTracks get() = queue.toList()
    var shuffle = false
    var repeat = false
    var loopQueue = false

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
            startNextSong(event.track)
        }

        // In order to loop the queue we just add every track back to the queue
        if (event.reason == TrackEndEvent.EndReason.FINISHED && loopQueue) {
            queue.add(event.track)
        }
    }

    suspend fun skip() = startNextSong()

    private suspend fun startNextSong(lastSong: Track? = null) {
        val nextTrack = when {
            lastSong != null && repeat -> lastSong
            shuffle -> {
                val index = Random.nextInt(queue.size)

                /* return */queue.removeAt(index)
            }
            else -> queue.poll()
        }
        link.player.playTrack(nextTrack)
    }

    fun clearQueue() = queue.clear()
}
