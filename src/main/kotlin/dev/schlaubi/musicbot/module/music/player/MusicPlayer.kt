package dev.schlaubi.musicbot.module.music.player

import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.module.settings.updateMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.random.Random
import kotlin.time.Duration

class MusicPlayer(internal val link: Link, private val guild: GuildBehavior, private val database: Database) :
    Link by link {
    private val queue = LinkedList<Track>()
    val queuedTracks get() = queue.toList()
    var shuffle = false
        set(value) {
            field = value
            updateMusicChannelMessage()
        }
    var repeat = false
        set(value) {
            field = value
            updateMusicChannelMessage()
        }
    var loopQueue = false
        set(value) {
            field = value
            if (value) { // if feature gets enabled
                // Queue the current track again, so it also gets looped
                player.playingTrack?.let {
                    // we directly add it because we don't need to perform any other tasks
                    // so essentially queueTrack would unnecessarily suspend
                    queue.add(it)
                }
            }
            updateMusicChannelMessage()
        }

    init {
        link.player.on(consumer = ::onTrackEnd)
    }

    val remainingQueueDuration: Duration
        get() {
            val remainingOfCurrentTrack =
                player.playingTrack?.length?.minus(Duration.milliseconds(player.position))
                    ?: Duration.milliseconds(0)

            val remainingQueue = queuedTracks
                .fold(Duration.seconds(0)) { acc, track -> acc + track.length }

            return remainingOfCurrentTrack + remainingQueue
        }

    val nextSongIsFirst: Boolean get() = queue.isEmpty() && link.player.playingTrack == null

    suspend fun queueTrack(force: Boolean, onTop: Boolean, tracks: Collection<Track>) {
        val isFirst = nextSongIsFirst
        val startIndex = if ((onTop || force) && queue.isNotEmpty()) 0 else queue.size
        queue.addAll(startIndex, tracks)

        if (force || isFirst) {
            startNextSong(force = force)
            waitForPlayerUpdate()
        }

        updateMusicChannelMessage()
    }

    private suspend fun onTrackEnd(event: TrackEndEvent) {
        if ((!repeat && queue.isEmpty()) && event.reason != TrackEndEvent.EndReason.REPLACED) {
            link.disconnectAudio()
            return updateMusicChannelMessage()
        }
        if (event.reason.mayStartNext) {
            startNextSong(event.track)
        }

        // In order to loop the queueTracks we just add every track back to the queueTracks
        if (event.reason == TrackEndEvent.EndReason.FINISHED && loopQueue) {
            queue.add(event.track)
        }

        if (repeat) {
            waitForPlayerUpdate()
        }

        updateMusicChannelMessage()
    }

    private suspend fun waitForPlayerUpdate() {
        // Delay .5 seconds to wait for player update event
        // Otherwise updateMusicChannelMessage() won't get the update
        delay(Duration.milliseconds(500))
    }

    suspend fun skip() {
        startNextSong()
        updateMusicChannelMessage()
    }

    private suspend fun startNextSong(lastSong: Track? = null, force: Boolean = false) {
        val nextTrack = when {
            lastSong != null && repeat -> lastSong
            !force && shuffle -> {
                val index = Random.nextInt(queue.size)

                /* return */queue.removeAt(index)
            }
            else -> queue.poll()
        }

        link.player.playTrack(nextTrack)
    }

    fun clearQueue() {
        queue.clear()

        updateMusicChannelMessage()
    }

    suspend fun stop() {
        player.stopTrack()
        link.disconnectAudio()
        clearQueue()
    }

    private fun updateMusicChannelMessage() {
        guild.kord.launch {
            updateMessage(guild.id, database, guild.kord, this@MusicPlayer)
        }
    }
}
