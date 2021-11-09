@file:OptIn(FiltersApi::class)

package dev.schlaubi.mikmusic.player

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.player.*
import dev.schlaubi.mikmusic.core.settings.SchedulerSettings
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class PersistentPlayerState(
    val guildId: Snowflake,
    val channelId: Snowflake,
    val queue: List<@Contextual QueuedTrack>,
    @Contextual // this is a playingTrack which contains the current position
    val currentTrack: QueuedTrack?,
    val filters: SerializableFilters?,
    val schedulerOptions: SchedulerSettings,
    val paused: Boolean,
    val position: Long
) {
    constructor(musicPlayer: MusicPlayer) : this(
        Snowflake(musicPlayer.guildId),
        Snowflake(musicPlayer.lastChannelId!!),
        musicPlayer.queuedTracks,
        musicPlayer.playingTrack,
        musicPlayer.filters,
        SchedulerSettings(musicPlayer.loopQueue, musicPlayer.repeat, musicPlayer.shuffle),
        musicPlayer.player.paused,
        musicPlayer.player.position
    )

    suspend fun applyToPlayer(musicPlayer: MusicPlayer) {
        filters?.applyToPlayer(musicPlayer.player)
        if (currentTrack != null) {
            musicPlayer.playingTrack = currentTrack
            musicPlayer.queueTrack(force = true, onTop = false, tracks = listOf(currentTrack))
        }
        musicPlayer.queueTrack(force = false, onTop = false, tracks = queue)
        musicPlayer.player.seekTo(position)
        if (paused) {
            musicPlayer.player.pause()
        }
    }
}

data class MutableFilters(
    override val karaoke: Filters.Karaoke? = null,
    override val timescale: Filters.Timescale? = null,
    override val tremolo: Filters.Tremolo? = null,
    override val vibrato: Filters.Vibrato? = null,
    override var volume: Float? = null
) : Filters


@Serializable
data class SerializableFilters(
    val karaoke: Filters.Karaoke?,
    val timescale: Filters.Timescale?,
    val tremolo: Filters.Tremolo?,
    val vibrato: Filters.Vibrato?,
    val volume: Float?
) {
    constructor(filters: Filters) : this(
        filters.karaoke,
        filters.timescale,
        filters.tremolo,
        filters.vibrato,
        filters.volume
    )

    suspend fun applyToPlayer(player: Player) {
        player.applyFilters {
            volume?.let { volume ->
                this.volume = volume
            }

            karaoke?.let { karaoke ->
                karaoke {
                    level = karaoke.level
                    monoLevel = karaoke.monoLevel
                    filterBand = karaoke.filterBand
                    filterWidth = karaoke.filterWidth
                }
            }

            timescale?.let { timescale ->
                timescale {
                    speed = timescale.speed
                    pitch = timescale.pitch
                    rate = timescale.rate
                }
            }

            tremolo?.let { tremolo ->
                tremolo {
                    frequency = tremolo.frequency
                    depth = tremolo.depth
                }
            }

            vibrato?.let { vibrato ->
                vibrato {
                    frequency = vibrato.frequency
                    depth = vibrato.depth
                }
            }
        }
    }
}

suspend fun SchedulerSettings.applyToPlayer(player: MusicPlayer) {
    if (shuffle != null) {
        player.shuffle = shuffle
    }
    if (loopQueue != null) {
        player.loopQueue = loopQueue
    }
    if (repeat != null) {
        player.repeat = repeat
    }

    if (player.filters?.volume != volume) {
        player.applyFilters {
            this.volume = volume
        }
    }
}
