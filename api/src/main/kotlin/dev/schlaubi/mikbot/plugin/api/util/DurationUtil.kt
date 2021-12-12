package dev.schlaubi.mikbot.plugin.api.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Duration

/**
 * Converts this [DateTimePeriod] to a [Duration].
 */
public fun DateTimePeriod.toDuration(): Duration {
    val now = Clock.System.now()
    val applied = now.plus(this, TimeZone.UTC)

    return applied - now
}
