package dev.schlaubi.musicbot.utils

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.live.AbstractLiveKordEntity
import dev.kord.core.live.on
import kotlinx.coroutines.CoroutineScope

fun Message.componentLive(coroutineScope: CoroutineScope) = ComponentLiveMessage(this, coroutineScope)

@OptIn(KordPreview::class)
class ComponentLiveMessage(private val message: Message, coroutineScope: CoroutineScope) :
    AbstractLiveKordEntity(message.kord, coroutineScope) {
    override val id: Snowflake
        get() = message.id

    override fun filter(event: Event): Boolean =
        (event as? ComponentInteractionCreateEvent)?.interaction?.message?.id == id

    override fun update(event: Event) = Unit

    fun onInteraction(consumer: suspend ComponentInteractionCreateEvent.() -> Unit) =
        on(this, consumer)
}
