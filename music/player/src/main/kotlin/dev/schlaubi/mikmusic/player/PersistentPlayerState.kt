package dev.schlaubi.mikmusic.player

import dev.arbjerg.lavalink.protocol.v4.Filters
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.api.types.QueuedTrack
import dev.schlaubi.mikmusic.api.types.SchedulerSettings
import dev.schlaubi.mikmusic.util.QueuedTrackJsonSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class PersistentPlayerState(
    val guildId: Snowflake,
    val channelId: Snowflake,
    val queue: List<@Serializable(with = QueuedTrackJsonSerializer::class) QueuedTrack>,
    @Contextual // this is a playingTrack which contains the current position
    val currentTrack: QueuedTrack?,
    val filters: Filters?,
    val schedulerOptions: SchedulerSettings,
    val paused: Boolean,
    val position: Duration,
    val autoPlayContext: AutoPlayContext?,
    val volume: Int,
) {
    constructor(musicPlayer: MusicPlayer) : this(
        Snowflake(musicPlayer.guildId),
        Snowflake(musicPlayer.lastChannelId!!),
        musicPlayer.queuedTracks,
        musicPlayer.playingTrack,
        musicPlayer.filters,
        SchedulerSettings(musicPlayer.loopQueue, musicPlayer.repeat, musicPlayer.shuffle),
        musicPlayer.player.paused,
        musicPlayer.player.positionDuration,
        musicPlayer.autoPlay,
        musicPlayer.player.volume
    )
}

fun SchedulerSettings.applyToPlayer(player: MusicPlayer) {
    if (shuffle != null) {
        player.shuffle = shuffle!!
    }
    if (loopQueue != null) {
        player.loopQueue = loopQueue!!
    }
    if (loop != null) {
        player.repeat = loop!!
    }
}
