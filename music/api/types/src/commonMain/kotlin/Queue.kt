package dev.schlaubi.mikmusic.api.types

import dev.kord.common.entity.optional.Optional
import kotlinx.serialization.Serializable

@Serializable
data class QueueAddRequest(
    val tracks: List<String>,
    val schedulerSettings: Optional<SchedulerSettings> = Optional.Missing(),
    val top: Boolean,
)

@Serializable
data class QueueRemoveRequest(
    val start: Int,
    val end: Int? = null,
)
