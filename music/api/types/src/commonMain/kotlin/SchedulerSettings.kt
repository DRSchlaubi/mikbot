package dev.schlaubi.mikmusic.api.types

import kotlinx.serialization.Serializable

class UnexclusiveSchedulingOptions() : RuntimeException()

interface SchedulingOptions {
    val shuffle: Boolean?
    val loop: Boolean?
    val loopQueue: Boolean?
}

@Serializable
data class SchedulerSettings(
    override val loopQueue: Boolean? = null,
    override val loop: Boolean? = null,
    override val shuffle: Boolean? = null,
    val volume: Float? = null,
) : SchedulingOptions {

    init {
        if ((loopQueue == true) xor (loop == true) xor (shuffle == true)) {
            throw UnexclusiveSchedulingOptions()
        }
    }

    fun merge(parent: SchedulerSettings) = SchedulerSettings(
        parent.loopQueue ?: loopQueue,
        parent.loop ?: loop,
        parent.shuffle ?: shuffle,
        parent.volume ?: volume
    )
}
