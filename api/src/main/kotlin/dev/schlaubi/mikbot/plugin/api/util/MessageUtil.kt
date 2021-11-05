package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.behavior.MessageBehavior
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Deletes this [MessageBehavior] after [duration].
 */
public suspend fun MessageBehavior.deleteAfterwards(duration: Duration = Duration.seconds(3)) {
    delay(duration)
    delete()
}
