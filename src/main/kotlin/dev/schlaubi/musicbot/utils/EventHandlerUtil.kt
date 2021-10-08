package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Responds with [CheckContext.message] if the check failed.
 */
@JvmName("respondIfFailedInInteraction")
suspend fun <T : InteractionCreateEvent> CheckContext<T>.respondIfFailed() = respondIfFailed {
    event.interaction.respondEphemeral { content = it }
}

/**
 * Responds with [CheckContext.message] if the check failed.
 */
@JvmName("respondIfFailedInMessageChannel")
suspend fun CheckContext<MessageCreateEvent>.respondIfFailed() = respondIfFailed {
    coroutineScope {
        launch { event.message.deleteAfterwards() }
        event.message.reply { content = it }.deleteAfterwards()
    }
}

@JvmName("respondIfFailedGeneric")
private suspend fun <T : Event> CheckContext<T>.respondIfFailed(respond: suspend (String) -> Unit) {
    if (!passed && message != null) {
        val content = message ?: return
        respond(content)
    }
}
