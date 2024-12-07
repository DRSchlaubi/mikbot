package dev.schlaubi.mikmusic.api.types

import dev.arbjerg.lavalink.protocol.v4.*
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.common.entity.optional.OptionalInt
import dev.schlaubi.lavakord.plugins.sponsorblock.model.YouTubeChapter
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class MinimalGuild(
    val id: Snowflake,
    val name: String,
    val icon: String?,
    val splash: String?,
)

@Serializable
data class MinimalUser(
    val id: Snowflake,
    val name: String,
    val avatar: String,
)

@Serializable
data class APIQueuedTrack(
    val user: MinimalUser,
    val track: Track,
    val chapters: List<YouTubeChapter>?,
)

@Serializable
data class PlayerState(
    val guild: MinimalGuild,
    val currentTrack: PlayingTrack?,
    val schedulerSettings: SchedulerSettings,
    val paused: Boolean,
) {
    @Serializable
    data class PlayingTrack(
        val track: APIQueuedTrack,
        val position: Duration,
        val volume: Int,
    )
}

@Serializable
data class Channel(
    val id: Snowflake,
    val guildId: Snowflake,
    val name: String,
)

@Serializable
data class Player(
    val state: PlayerState,
    val queue: List<APIQueuedTrack>,
    val voiceState: VoiceState,
) {
    @Serializable
    data class VoiceState(
        val channelMismatch: Boolean,
        val channel: Channel?,
        val playerAvailable: Boolean,
    )
}

@Serializable
data class UpdatablePlayerState(
    val channel: Optional<Snowflake> = Optional.Missing(),
    val track: Optional<String> = Optional.Missing(),
    val position: Optional<Duration> = Optional.Missing(),
    val volume: OptionalInt = OptionalInt.Missing,
    val paused: OptionalBoolean = OptionalBoolean.Missing,
    val schedulerSettings: Optional<SchedulerSettings> = Optional.Missing(),
) {
    @Serializable
    data class SchedulerSettings(
        val loopQueue: OptionalBoolean = OptionalBoolean.Missing,
        val repeat: OptionalBoolean = OptionalBoolean.Missing,
        val shuffle: OptionalBoolean = OptionalBoolean.Missing,
    )

    fun toLavalinkUpdate() = PlayerUpdate(
        position = position.toOmissible().map { it.inWholeMilliseconds },
        volume = volume.toOmissible(),
        paused = paused.toOmissible()
    )
}

@Suppress("UNCHECKED_CAST")
fun <T : Any?> Optional<T>.toOmissible(): Omissible<T> = when (this) {
    is Optional.Value<*>, is Optional.Null<*> -> Omissible.Present(value as T)
    is Optional.Missing<*> -> Omissible.Omitted()
}

fun OptionalInt.toOmissible(): Omissible<Int> = when (this) {
    is OptionalInt.Missing -> Omissible.Omitted()
    is OptionalInt.Value -> Omissible.Present(value)
}

fun OptionalBoolean.toOmissible(): Omissible<Boolean> = when (this) {
    is OptionalBoolean.Missing -> Omissible.Omitted()
    is OptionalBoolean.Value -> Omissible.Present(value)
}
