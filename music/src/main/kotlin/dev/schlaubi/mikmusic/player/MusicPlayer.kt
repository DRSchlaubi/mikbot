package dev.schlaubi.mikmusic.player

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.inmo.krontab.buildSchedule
import dev.inmo.krontab.doInfinity
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Filters
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.innerttube.requestVideoChaptersById
import dev.schlaubi.mikmusic.musicchannel.updateMessage
import dev.schlaubi.mikmusic.sponsorblock.checkAndSkipSponsorBlockSegments
import dev.schlaubi.mikmusic.sponsorblock.deleteSponsorBlockCache
import dev.schlaubi.mikmusic.util.youtubeId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MusicPlayer(internal val link: Link, private val guild: GuildBehavior) :
    Link by link, KordExKoinComponent {
    private var queue = LinkedList<QueuedTrack>()
    val queuedTracks get() = queue.toList()
    var filters: SerializableFilters? = null
    var playingTrack: QueuedTrack? = null
    var disableMusicChannel: Boolean = false
        private set
    private val translationsProvider: TranslationsProvider by inject()

    private var sponsorBlockJob: Job? = null
    private var chapterUpdater: Job? = null
    private var leaveTimeout: Job? = null

    init {
        guild.kord.launch {
            val settings = MusicSettingsDatabase.findGuild(guild)

            settings.defaultSchedulerSettings?.applyToPlayer(this@MusicPlayer)

            if (settings.useSponsorBlock) {
                launchSponsorBlockJob()
            }
        }

        link.player.on(consumer = ::onTrackEnd)
        link.player.on(consumer = ::onTrackStart)
    }

    suspend fun getChannel() = link.lastChannelId
        ?.let { guild.kord.getChannelOf<VoiceChannel>(Snowflake(it)) }

    fun updateMusicChannelState(to: Boolean) {
        if (to) {
            queue.clear()
            playingTrack = null
            updateMusicChannelMessage()
        }
        disableMusicChannel = to
    }

    fun launchSponsorBlockJob() {
        if (sponsorBlockJob != null) {
            return
        }
        sponsorBlockJob = guild.kord.launch {
            buildSchedule("/1 * * * *").doInfinity {
                player.playingTrack?.checkAndSkipSponsorBlockSegments(player)
            }
        }
    }

    fun cancelSponsorBlockJob() {
        sponsorBlockJob?.cancel()
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
                player.playingTrack?.length?.minus(player.position.milliseconds)
                    ?: 0.milliseconds

            val remainingQueue = queuedTracks
                .fold(0.seconds) { acc, track -> acc + track.track.length }

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

    suspend fun applyFilters(builder: Filters.() -> Unit) {
        val filters = MutableFilters().apply(builder)
        player.applyFilters(builder)
        this.filters = SerializableFilters(filters)
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun onTrackStart(event: TrackStartEvent) {
        leaveTimeout?.cancel()
        updateMusicChannelMessage()
        applySponsorBlock(event)

        guild.kord.launch {
            val youtubeId = event.track.youtubeId ?: return@launch
            val chapters = requestVideoChaptersById(youtubeId)
            if (chapters.isNotEmpty()) {
                val queuedBy = playingTrack?.queuedBy ?: Snowflake(0)
                playingTrack = ChapterQueuedTrack(event.track, queuedBy, chapters)
                updateMusicChannelMessage()
                restartChapterUpdater()
            }
        }
    }

    private fun applySponsorBlock(event: TrackStartEvent) {
        if (sponsorBlockJob == null) {
            return
        }
        guild.kord.launch {
            event.track.checkAndSkipSponsorBlockSegments(player)
        }
    }

    private suspend fun onTrackEnd(event: TrackEndEvent) {
        event.track.deleteSponsorBlockCache()
        if ((!repeat && !loopQueue && queue.isEmpty()) && event.reason != TrackEndEvent.EndReason.REPLACED) {
            leaveTimeout = lavakord.launch {
                delay(MusicSettingsDatabase.findGuild(guild).leaveTimeout)
                stop()
            }
            return
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
        delay(500.milliseconds)
    }

    suspend fun skip(to: Int = 1) {
        if (to > 1) {
            // Drop every track, but the skip to track and then start the next track
            queue = LinkedList(queue.drop(to - 1))
        }
        startNextSong()
        updateMusicChannelMessage()
    }

    suspend fun skipChapter() {
        val chapterTrack = (playingTrack as? ChapterQueuedTrack) ?: return
        val chapter = chapterTrack.nextChapter()
        player.seekTo(chapter.startTime)
        restartChapterUpdater(chapter.startTime)
        updateMusicChannelMessage()
    }

    private suspend fun restartChapterUpdater(position: Duration? = null) {
        chapterUpdater?.cancel()
        chapterUpdater = guild.kord.launch {
            val chapterTrack = (playingTrack as? ChapterQueuedTrack) ?: return@launch
            if (chapterTrack.isOnLast) {
                return@launch
            }

            val nextChapter = chapterTrack.chapters[chapterTrack.chapterIndex + 1]

            val diff = nextChapter.startTime - (position ?: player.positionDuration)
            delay(diff)

            chapterTrack.nextChapter()
            updateMusicChannelMessage()
            restartChapterUpdater(nextChapter.startTime)
        }
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

    suspend fun pause(pause: Boolean = !player.paused) {
        player.pause(pause)

        delay(500) // Wait for change to propagate
        updateMusicChannelMessage()
    }

    suspend fun stop() {
        player.stopTrack()
        link.disconnectAudio()
        clearQueue()
        playingTrack = null
        updateMusicChannelMessage()
    }

    fun updateMusicChannelMessage() {
        if (!disableMusicChannel) {
            guild.kord.launch {
                updateMessage(
                    guild.id,
                    guild.kord,
                    this@MusicPlayer,
                    translationsProvider = translationsProvider
                )
            }
        }
    }
}

private fun <T : MutableList<*>> T.countRemoves(mutator: T.() -> Unit): Int {
    val currentSize = size
    apply(mutator)
    return currentSize - size
}
