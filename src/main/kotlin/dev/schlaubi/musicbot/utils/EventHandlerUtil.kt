package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent

@JvmName("respondIfFailedInInteraction")
suspend fun <T : InteractionCreateEvent> CheckContext<T>.respondIfFailed() = respondIfFailed {
    event.interaction.respondEphemeral { content = it }
}

@JvmName("respondIfFailedInMessageChannel")
suspend fun CheckContext<MessageCreateEvent>.respondIfFailed() = respondIfFailed {
    event.message.channel.createMessage(it)
}

@JvmName("respondIfFailedGeneric")
private suspend fun <T : Event> CheckContext<T>.respondIfFailed(respond: suspend (String) -> Unit) {
    if (!passed && message != null) {
        val content = message ?: return
        respond(content)
    }
}
