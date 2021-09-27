package dev.schlaubi.musicbot.utils

import dev.kord.core.behavior.channel.MessageChannelBehavior
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

suspend fun <T> MessageChannelBehavior.typeUntilDone(task: suspend () -> T) = coroutineScope {
    val typing = launch {
        while (true) {
            type()
            delay(Duration.seconds(8))
        }
    }

    val result = task()
    typing.cancel()
    return@coroutineScope result
}
