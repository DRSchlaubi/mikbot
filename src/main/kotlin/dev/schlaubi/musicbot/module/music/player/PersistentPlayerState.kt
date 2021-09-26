@file:OptIn(FiltersApi::class)

package dev.schlaubi.musicbot.module.music.player

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.player.Filters
import dev.schlaubi.lavakord.audio.player.FiltersApi
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.lavakord.audio.player.karaoke
import dev.schlaubi.lavakord.audio.player.timescale
import dev.schlaubi.lavakord.audio.player.tremolo
import dev.schlaubi.lavakord.audio.player.vibrato
import dev.schlaubi.musicbot.module.settings.SchedulerSettings
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class PersistentPlayerState(
    val guildId: Snowflake,
    val channelId: Snowflake,
    val queue: List<@Contextual Track>,
    @Contextual //this is a playingTrack which contains the current position
    val currenTrack: Track?,
    val filters: SerializableFilters?,
    val schedulerOptions: SchedulerSettings,
    val paused: Boolean,
    val position: Long
) {
    constructor(musicPlayer: MusicPlayer) : this(
        Snowflake(musicPlayer.guildId),
        Snowflake(musicPlayer.lastChannelId!!),
        musicPlayer.queuedTracks,
        musicPlayer.player.playingTrack,
        musicPlayer.filters,
        SchedulerSettings(musicPlayer.shuffle, musicPlayer.loopQueue, musicPlayer.repeat),
        musicPlayer.player.paused,
        musicPlayer.player.position
    )

    suspend fun applyToPlayer(musicPlayer: MusicPlayer) {
        filters?.applyToPlayer(musicPlayer.player)
        if (currenTrack != null) {
            musicPlayer.queueTrack(force = true, onTop = false, tracks = listOf(currenTrack))
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

@JvmRecord
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

    if(player.filters?.volume != volume) {
        player.applyFilters {
            this.volume = volume
        }
    }
}
