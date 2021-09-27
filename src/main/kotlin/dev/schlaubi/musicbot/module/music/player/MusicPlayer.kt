package dev.schlaubi.musicbot.module.music.player

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Filters
import dev.schlaubi.lavakord.audio.player.FiltersApi
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.module.settings.updateMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.LinkedList
import kotlin.random.Random
import kotlin.time.Duration

class MusicPlayer(internal val link: Link, private val guild: GuildBehavior, private val database: Database) :
    Link by link, KoinComponent {
    private var queue = LinkedList<QueuedTrack>()
    val queuedTracks get() = queue.toList()
    var filters: SerializableFilters? = null
    var playingTrack: QueuedTrack? = null
    private val translationsProvider: TranslationsProvider by inject()

    init {
        guild.kord.launch {
            val settings = database.guildSettings.findGuild(guild)

            settings.defaultSchedulerSettings?.applyToPlayer(this@MusicPlayer)
        }

        link.player.on(consumer = ::onTrackEnd)
        link.player.on(consumer = ::onTrackStart)
    }

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
            updateMusicChannelMessage()
        }

    val remainingQueueDuration: Duration
        get() {
            val remainingOfCurrentTrack =
                player.playingTrack?.length?.minus(Duration.milliseconds(player.position))
                    ?: Duration.milliseconds(0)

            val remainingQueue = queuedTracks
                .fold(Duration.seconds(0)) { acc, track -> acc + track.track.length }

            return remainingOfCurrentTrack + remainingQueue
        }

    val nextSongIsFirst: Boolean get() = queue.isEmpty() && link.player.playingTrack == null

    fun moveQueuedEntry(from: Int, to: Int, swap: Boolean): Track? {
        val song = queue.getOrNull(from) ?: return null
        if (swap) {
            val toValue = queue.getOrNull(to) ?: return null
            queue[to] = song
            queue[from] = toValue
        } else {
            queue.add(to, song)
            queue.removeAt(from)
        }

        updateMusicChannelMessage()
        return song.track
    }

    fun removeQueueEntry(index: Int): Track? {
        val track = runCatching { queue.removeAt(index) }.getOrNull() ?: return null
        updateMusicChannelMessage()
        return track.track
    }

    fun removeQueueEntries(range: IntRange): Int {
        val queueSize = queue.size
        if (range.first >= 0 && range.last <= queue.size) {
            val before = queue.subList(0, range.first - 1) // inclusive
            val after = queue.subList(range.last, queueSize)
            val combined = before + after
            queue = LinkedList(combined)

            updateMusicChannelMessage()
        }

        return queueSize - queue.size
    }

    fun removeDoubles(): Int {
        val removes = queue.countRemoves {
            val tracks = mutableListOf<String>()
            queue.removeIf {
                if (it.track.track in tracks) {
                    true
                } else {
                    tracks.add(it.track.track)
                    false
                }
            }
        }

        updateMusicChannelMessage()

        return removes
    }

    fun removeFromUser(predicate: (Snowflake) -> Boolean): Int {
        val removes = queue.countRemoves {
            queue.removeIf { predicate(it.queuedBy) }
        }

        updateMusicChannelMessage()

        return removes
    }

    suspend fun queueTrack(force: Boolean, onTop: Boolean, tracks: Collection<QueuedTrack>) {
        val isFirst = nextSongIsFirst
        val startIndex = if ((onTop || force) && queue.isNotEmpty()) 0 else queue.size
        queue.addAll(startIndex, tracks)

        if (force || isFirst) {
            startNextSong(force = force)
            waitForPlayerUpdate()
        }

        updateMusicChannelMessage()
    }

    @OptIn(FiltersApi::class)
    suspend fun applyFilters(builder: Filters.() -> Unit) {
        val filters = MutableFilters().apply(builder)
        player.applyFilters(builder)
        this.filters = SerializableFilters(filters)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onTrackStart(event: TrackStartEvent) {
        updateMusicChannelMessage()
    }

    private suspend fun onTrackEnd(event: TrackEndEvent) {
        if ((!repeat && !loopQueue && queue.isEmpty()) && event.reason != TrackEndEvent.EndReason.REPLACED) {
            link.disconnectAudio()
            return updateMusicChannelMessage()
        }

        // In order to loop the queueTracks we just add every track back to the queueTracks
        if (event.reason == TrackEndEvent.EndReason.FINISHED && loopQueue && playingTrack != null) {
            queue.add(playingTrack!!)
        }

        if (event.reason.mayStartNext) {
            startNextSong(event.track)
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

    suspend fun skip(to: Int = 1) {
        if (to > 1) {
            // Drop every track, but the skip to track and then start the next track
            queue = LinkedList(queue.drop(to - 1))
        }
        startNextSong()
        updateMusicChannelMessage()
    }

    private suspend fun startNextSong(lastSong: Track? = null, force: Boolean = false) {
        val nextTrack = when {
            lastSong != null && repeat -> playingTrack!!
            !force && (shuffle || (loopQueue && queue.isEmpty())) -> {
                val index = Random.nextInt(queue.size)

                /* return */queue.removeAt(index)
            }
            else -> queue.poll()
        }

        playingTrack = nextTrack
        link.player.playTrack(nextTrack.track)
    }

    fun toState(): PersistentPlayerState = PersistentPlayerState(this)

    fun clearQueue() {
        queue.clear()

        updateMusicChannelMessage()
    }

    suspend fun stop() {
        player.stopTrack()
        link.disconnectAudio()
        clearQueue()
    }

    fun updateMusicChannelMessage() {
        guild.kord.launch {
            updateMessage(guild.id, database, guild.kord, this@MusicPlayer, translationsProvider = translationsProvider)
        }
    }
}

private fun <T : MutableList<*>> T.countRemoves(mutator: T.() -> Unit): Int {
    val currentSize = size
    apply(mutator)
    return currentSize - size
}

@JvmRecord
data class QueuedTrack(val track: Track, val queuedBy: Snowflake)
