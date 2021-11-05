package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.live.AbstractLiveKordEntity
import dev.kord.core.live.on
import kotlinx.coroutines.Job

public fun Message.componentLive(): ComponentLiveMessage = ComponentLiveMessage(this)

@OptIn(KordPreview::class)
public class ComponentLiveMessage(private val message: Message) :
    AbstractLiveKordEntity(message.kord) {
    override val id: Snowflake
        get() = message.id

    override fun filter(event: Event): Boolean =
        (event as? ComponentInteractionCreateEvent)?.interaction?.message?.id == id

    override fun update(event: Event): Unit = Unit

    public fun onInteraction(consumer: suspend ComponentInteractionCreateEvent.() -> Unit): Job =
        on(this, consumer)
}
