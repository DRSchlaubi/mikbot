package dev.schlaubi.mikmusic.player

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Filters
import dev.schlaubi.lavakord.audio.player.PlayOptions
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.model.ChapterStartedEvent
import dev.schlaubi.lavakord.plugins.sponsorblock.model.ChaptersLoadedEvent
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.disableSponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.getSponsorblockCategories
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.musicchannel.updateMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.inject
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal data class SavedTrack(
    val track: QueuedTrack,
    val position: Duration,
    val filters: Filters,
    val volume: Int,
    val pause: Boolean,
)

class MusicPlayer(val link: Link, private val guild: GuildBehavior) : Link by link, KordExKoinComponent {

    private val lock = Mutex()

    private var queue = LinkedList<QueuedTrack>()
    val queuedTracks get() = queue.toList()
    val canSkip: Boolean
        get() = queuedTracks.isNotEmpty() || !autoPlay?.songs.isNullOrEmpty()
    var filters: SerializableFilters? = null
    var playingTrack: QueuedTrack? = null
    var disableMusicChannel: Boolean = false
        private set
    private val translationsProvider: TranslationsProvider by inject()

    private var leaveTimeout: Job? = null
    internal var autoPlay: AutoPlayContext? = null
    internal var savedTrack: SavedTrack? = null
    private var dontQueue = false

    init {
        guild.kord.launch {
            val settings = MusicSettingsDatabase.findGuild(guild)

            settings.defaultSchedulerSettings?.applyToPlayer(this@MusicPlayer)
        }

        updateSponsorBlock()

        link.player.on(consumer = ::onTrackEnd)
        link.player.on(consumer = ::onTrackStart)
        link.player.on(consumer = ::onChaptersLoaded)
        link.player.on(consumer = ::onChapterStarted)
    }

    private fun updateSponsorBlock() = guild.kord.launch {
        val settings = MusicSettingsDatabase.findGuild(guild)
        val categories = player.getSponsorblockCategories()

        if (categories.isEmpty() && settings.useSponsorBlock) {
            player.putSponsorblockCategories(
                Category.MusicOfftopic,
                Category.Filler,
                Category.Selfpromo,
                Category.Sponsor
            )
        } else if (categories.isNotEmpty()) {
            player.disableSponsorblock()
        }
    }

    suspend fun getChannel() = link.lastChannelId
        ?.let { guild.kord.getChannelOf<VoiceChannel>(Snowflake(it)) }

    @Suppress("unused") // used by other plugins
    fun updateMusicChannelState(to: Boolean) {
        if (to) {
            queue.clear()
            playingTrack = null
            updateMusicChannelMessage()
        }
        disableMusicChannel = to
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
                player.playingTrack?.info?.length?.minus(player.position)
                    ?.toDuration(DurationUnit.MILLISECONDS)
                    ?: 0.milliseconds

            val remainingQueue = queuedTracks
                .fold(0.seconds) { acc, track -> acc + track.track.info.length.toDuration(DurationUnit.MILLISECONDS) }

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
                if (it.track.info.identifier in tracks) {
                    true
                } else {
                    tracks.add(it.track.info.identifier)
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

    suspend fun queueTrack(
        force: Boolean,
        onTop: Boolean,
        tracks: Collection<QueuedTrack>,
        position: Duration? = null,
    ) = lock.withLock {
        val isFirst = nextSongIsFirst
        require(isFirst || position == null) { "Can only specify position if nextSong is first" }
        val startIndex = if ((onTop || force) && queue.isNotEmpty()) 0 else queue.size
        queue.addAll(startIndex, tracks)

        if ((force || isFirst) && !dontQueue) {
            startNextSong(force = force, position = position)
            waitForPlayerUpdate()
        }

        updateMusicChannelMessage()
    }

    @Suppress("unused") // used by Tonbrett
    suspend fun injectTrack(
        identifier: String,
        noReplace: Boolean = false,
        playOptionsBuilder: PlayOptions.() -> Unit,
    ) = lock.withLock {
        dontQueue = true
        val currentTrack = playingTrack
        if (currentTrack != null && !noReplace) {
            val currentPosition = player.positionDuration

            savedTrack = SavedTrack(currentTrack, currentPosition, player.filters, player.volume, player.paused)
        }
        link.player.searchAndPlayTrack(identifier, playOptionsBuilder)
    }

    suspend fun applyFilters(builder: Filters.() -> Unit) {
        val filters = MutableFilters().apply(builder)
        player.applyFilters(builder)
        this.filters = SerializableFilters(filters)
    }

    private fun onChaptersLoaded(event: ChaptersLoadedEvent) {
        val playingTrack = playingTrack ?: return
        val queuedBy = playingTrack.queuedBy
        this.playingTrack = ChapterQueuedTrack(playingTrack.track, queuedBy, event.chapters)
        updateMusicChannelMessage()
    }

    private fun onChapterStarted(event: ChapterStartedEvent) {
        val chapterTrack = playingTrack as? ChapterQueuedTrack ?: return
        chapterTrack.skipTo(event.chapter.start, event.chapter.name)
        updateMusicChannelMessage()
    }

    private fun onTrackStart(@Suppress("UNUSED_PARAMETER") event: TrackStartEvent) {
        leaveTimeout?.cancel()
        updateMusicChannelMessage()
    }

    private suspend fun onTrackEnd(event: TrackEndEvent) = lock.withLock {
        if (dontQueue) {
            dontQueue = event.reason == Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.REPLACED
        }
        if (savedTrack != null && event.reason != Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.REPLACED) {
            val track = savedTrack ?: return@onTrackEnd
            savedTrack = null
            player.playTrack(track.track.track) {
                position = track.position
                filters = track.filters
                volume = track.volume
                pause = track.pause
            }
            return@onTrackEnd
        }
        if ((!repeat && !loopQueue && queue.isEmpty()) && event.reason != Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.REPLACED) {
            val autoPlayTrack = findNextAutoPlayedSong(event.track)
            if (autoPlayTrack != null) {
                queue.add(SimpleQueuedTrack(autoPlayTrack, guild.kord.selfId))
            } else {
                playingTrack = null
                updateMusicChannelMessage()
                leaveTimeout = lavakord.launch {
                    delay(MusicSettingsDatabase.findGuild(guild).leaveTimeout)
                    stop()
                }
                return@onTrackEnd
            }
        }

        // In order to loop the queueTracks we just add every track back to the queueTracks
        if (event.reason == Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED && loopQueue && playingTrack != null) {
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

    suspend fun skip(to: Int = 1) = lock.withLock {
        val maybeSavedTrack = savedTrack
        if (to > 1) {
            // Drop every track, but the skip to track and then start the next track
            queue = LinkedList(queue.drop(to - 1))
        } else if (maybeSavedTrack != null) {
            savedTrack = null
            player.playTrack(maybeSavedTrack.track.track) {
                position = maybeSavedTrack.position
                filters = maybeSavedTrack.filters
                volume = maybeSavedTrack.volume
            }
            return@skip
        }
        if (canSkip) {
            startNextSong()
            updateMusicChannelMessage()
        } else {
            stop()
        }
    }

    suspend fun skipChapter() = lock.withLock {
        val chapterTrack = (playingTrack as? ChapterQueuedTrack) ?: return@skipChapter
        val chapter = chapterTrack.nextChapter()
        player.seekTo(chapter.start)
        updateMusicChannelMessage()
    }

    // called under lock
    private suspend fun startNextSong(lastSong: Track? = null, force: Boolean = false, position: Duration? = null) {
        updateSponsorBlock()
        val nextTrack: QueuedTrack? = when {
            lastSong != null && repeat -> playingTrack!!
            !force && (shuffle || (loopQueue && queue.isEmpty())) -> {
                val index = Random.nextInt(queue.size)

                /* return */queue.removeAt(index)
            }

            else -> queue.poll()
        }
        if (nextTrack == null) {
            updateMusicChannelMessage()
            return
        }

        playingTrack = nextTrack
        link.player.playTrack(nextTrack.track) {
            this.position = position
        }
    }

    fun toState(): PersistentPlayerState = PersistentPlayerState(this)

    fun clearQueue() {
        queue.clear()

        updateMusicChannelMessage()
    }

    suspend fun pause(pause: Boolean = !player.paused) = lock.withLock {
        player.pause(pause)

        delay(500) // Wait for change to propagate
        updateMusicChannelMessage()
    }

    suspend fun stop() {
        player.stopTrack()
        link.disconnectAudio()
        clearQueue()
        autoPlay = null
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
