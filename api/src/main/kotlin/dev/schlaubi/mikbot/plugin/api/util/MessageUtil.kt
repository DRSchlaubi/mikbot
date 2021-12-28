package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.behavior.MessageBehavior
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Deletes this [MessageBehavior] after [duration].
 */
public suspend fun MessageBehavior.deleteAfterwards(duration: Duration = 3.seconds) {
    delay(duration)
    delete()
}
