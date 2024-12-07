package dev.schlaubi.mikmusic.api.types

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Event {
    val guildId: Snowflake
}

@Serializable
@SerialName("player_update")
data class PlayerUpdateEvent(
    val state: PlayerState,
    val queue: List<APIQueuedTrack>,
    val time: Long,
    override val guildId: Snowflake,
) : Event

@Serializable
@SerialName("queue_update")
data class QueueUpdateEvent(val queue: List<APIQueuedTrack>, override val guildId: Snowflake) : Event

@Serializable
@SerialName("voice_state_update")
data class VoiceStateUpdateEvent(
    override val guildId: Snowflake,
    val state: Player.VoiceState,
) : Event
