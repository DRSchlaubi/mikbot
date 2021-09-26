package dev.schlaubi.musicbot.module.settings

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class SchedulerSettings(
    val loopQueue: Boolean? = null,
    val repeat: Boolean? = null,
    val shuffle: Boolean? = null,
    val volume: Float? = null
) {
    fun merge(parent: SchedulerSettings) = SchedulerSettings(
        parent.loopQueue ?: loopQueue,
        parent.repeat ?: repeat,
        parent.shuffle ?: shuffle,
        parent.volume ?: volume
    )
}
