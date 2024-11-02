package dev.schlaubi.mikmusic.player

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.PlayerUpdate
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.toOmissible
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.KordObject
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.lavakord.UnsafeRestApi
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
import dev.schlaubi.lavakord.rest.getPlayer
import dev.schlaubi.lavakord.rest.updatePlayer
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.musicchannel.updateMessage
import dev.schlaubi.mikmusic.player.queue.SchedulingOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.get
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
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

class MusicPlayer(val link: Link, private val guild: GuildBehavior) : Link by link, KordExKoinComponent, CoroutineScope, KordObject {

    private val lock = Mutex()

    var queue = Queue()
    val queuedTracks get() = queue.tracks
    val canSkip: Boolean
        get() = queuedTracks.isNotEmpty() || !autoPlay?.songs.isNullOrEmpty()
    var filters: dev.arbjerg.lavalink.protocol.v4.Filters? = null
    var playingTrack: QueuedTrack? = null
    var disableMusicChannel: Boolean = false
        private set
    private val translationsProvider: TranslationsProvider by inject()

    private var leaveTimeout: Job? = null
    internal var autoPlay: AutoPlayContext? = null
    internal var savedTrack: SavedTrack? = null
    private var dontQueue = false
    override val kord: Kord
        get() = guild.kord
    val hasAutoPlay: Boolean
        get() = !autoPlay?.songs.isNullOrEmpty()
    val autoPlayTrackCount
        get() = autoPlay?.songs?.size ?: 0
    private val musicChannelUpdateFlow = MutableSharedFlow<Unit>(0, 1, BufferOverflow.DROP_OLDEST)
    override val coroutineContext: CoroutineContext
        get() = kord.coroutineContext + SupervisorJob()

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
        @OptIn(FlowPreview::class)
        musicChannelUpdateFlow.debounce(5.seconds).onEach {
            updateMusicChannelMessage()
        }.launchIn(this)
    }

    private fun updateSponsorBlock() = guild.kord.launch {
        val settings = MusicSettingsDatabase.findGuild(guild)
        val categories = runCatching { player.getSponsorblockCategories() }.getOrElse { emptyList() }

        if (categories.isEmpty() && settings.useSponsorBlock) {
            player.putSponsorblockCategories(
                Category.MusicOfftopic,
                Category.Filler,
                Category.Selfpromo,
                Category.Sponsor
            )
        } else if (categories.isNotEmpty() && !settings.useSponsorBlock) {
            player.disableSponsorblock()
        }
    }

    suspend fun getChannel() = link.lastChannelId
        ?.let { guild.kord.getChannelOf<VoiceChannel>(Snowflake(it)) }

    @Suppress("unused") // used by other plugins
    fun updateMusicChannelState(to: Boolean) {
        disableMusicChannel = to
        queueMusicChannelMessageUpdate()
    }

    var shuffle: Boolean
        get() = queue.shuffle
        set(value) {
            queue.shuffle = value
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

    val remainingTrackDuration: Duration
        get() = player.playingTrack?.info?.length?.minus(player.position)
            ?.toDuration(DurationUnit.MILLISECONDS)
            ?: 0.milliseconds

    val remainingQueueDuration: Duration
        get() {
            val remainingQueue = queuedTracks
                .fold(0.seconds) { acc, track -> acc + track.track.info.length.toDuration(DurationUnit.MILLISECONDS) }

            return remainingTrackDuration + remainingQueue
        }

    val nextSongIsFirst: Boolean get() = queue.isEmpty() && link.player.playingTrack == null

    suspend fun queueTrack(
        force: Boolean,
        onTop: Boolean,
        tracks: Collection<QueuedTrack>,
        position: Duration? = null,
        schedulingOptions: SchedulingOptions? = null
    ) = lock.withLock {
        val isFirst = nextSongIsFirst
        require(isFirst || position == null) { "Can only specify position if nextSong is first" }
        queue.addTracks(tracks, onTop || force)

        queue.shuffle = schedulingOptions?.shuffle ?: queue.shuffle
        loopQueue = schedulingOptions?.loopQueue ?: loopQueue
        repeat = schedulingOptions?.loop ?: repeat

        if ((force || isFirst) && !dontQueue) {
            startNextSong(position = position)
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

    @OptIn(UnsafeRestApi::class)
    suspend fun changeVolume(newVolume: Int) {
        node.updatePlayer(guildId, request = PlayerUpdate(volume = newVolume.toOmissible()))
    }

    @OptIn(UnsafeRestApi::class)
    suspend fun applyState(state: PersistentPlayerState) {
        queue = Queue(state.queue.toMutableList())
        playingTrack = state.currentTrack
        filters = state.filters
        autoPlay = state.autoPlayContext

        val track = state.currentTrack?.track?.encoded
        val position = state.position.inWholeMilliseconds.takeIf { track != null }

        node.updatePlayer(
            guildId, request = PlayerUpdate(
                encodedTrack = track.toOmissible(),
                position = position.toOmissible(),
                volume = state.volume.toOmissible(),
                filters = filters.toOmissible(),
                paused = state.paused.toOmissible()
            )
        )
        updateMusicChannelMessage()
    }

    suspend fun applyFilters(builder: Filters.() -> Unit) {
        player.applyFilters(builder)
        this.filters = node.getPlayer(guildId).filters
    }

    private fun onChaptersLoaded(event: ChaptersLoadedEvent) {
        val playingTrack = playingTrack ?: return
        val queuedBy = playingTrack.queuedBy
        this.playingTrack = ChapterQueuedTrack(playingTrack.track, queuedBy, event.chapters)
        queueMusicChannelMessageUpdate()
    }

    private fun onChapterStarted(event: ChapterStartedEvent) {
        val chapterTrack = playingTrack as? ChapterQueuedTrack ?: return
        chapterTrack.skipTo(event.chapter.start, event.chapter.name)
        updateMusicChannelMessage()
    }

    private fun onTrackStart(@Suppress("unused") event: TrackStartEvent) {
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
            if (autoPlay == null) {
                playingTrack = null
                updateMusicChannelMessage()
                startLeaveTimeout()
                return@onTrackEnd
            }
        }

        // In order to loop the queueTracks we just add every track back to the queueTracks
        if (event.reason == Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED && loopQueue && playingTrack != null) {
            queue.addTracks(playingTrack!!)
        }

        if (event.reason.mayStartNext) {
            startNextSong(event.track)
        }

        if (repeat) {
            waitForPlayerUpdate()
        }

        updateMusicChannelMessage()
    }

    fun startLeaveTimeout() {
        leaveTimeout = lavakord.launch {
            delay(MusicSettingsDatabase.findGuild(guild).leaveTimeout)
            stop()
        }
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
            queue.drop(to - 1)
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
    private suspend fun startNextSong(lastSong: Track? = null, position: Duration? = null) {
        updateSponsorBlock()
        val autoPlayTrack = findNextAutoPlayedSong()
        if (autoPlayTrack != null) {
            queue.addTracks(SimpleQueuedTrack(autoPlayTrack, guild.kord.selfId))
        }
        if (queue.isEmpty()) {
            updateMusicChannelMessage()
            return
        }
        val nextTrack: QueuedTrack = when {
            lastSong != null && repeat -> playingTrack!!
            else -> queue.poll()
        }

        playingTrack = nextTrack
        link.player.playTrack(nextTrack.track) {
            this.position = position
        }
    }

    fun toState(): PersistentPlayerState = PersistentPlayerState(this)


    suspend fun pause(pause: Boolean = !player.paused) = lock.withLock {
        player.pause(pause)

        waitForPlayerUpdate() // Wait for change to propagate
        updateMusicChannelMessage()
    }

    suspend fun start() {
        if (playingTrack == null) {
            startNextSong()
        }
    }

    suspend fun stop() {
        player.stopTrack()
        link.disconnectAudio()
        queue.clear()
        autoPlay = null
        playingTrack = null
        queueMusicChannelMessageUpdate()
        coroutineContext.cancel()

        get<ExtensibleBot>().findExtension<MusicModule>()?.unregister(guild.id)
    }

    private fun queueMusicChannelMessageUpdate() {
        musicChannelUpdateFlow.tryEmit(Unit)
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
