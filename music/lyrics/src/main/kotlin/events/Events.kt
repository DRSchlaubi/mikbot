package dev.schlaubi.mikmusic.lyrics.events

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface Event

@SerialName("player_update")
@Serializable
data class PlayerStateUpdateEvent(
    val playing: Boolean,
    val position: Long,
    val timestamp: Instant
) : Event {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerStateUpdateEvent

        if (playing != other.playing) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playing.hashCode()
        result = 31 * result + position.hashCode()
        return result
    }
}

@SerialName("player_stopped")
@Serializable
data object PlayerStoppedEvent : Event

@SerialName("next_track")
@Serializable
data class NextTrackEvent(val startPosition: Long) : Event
