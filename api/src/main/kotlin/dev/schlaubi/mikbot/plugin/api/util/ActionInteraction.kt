package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.behavior.interaction.*
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.Interaction
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

/**
 * Acknowledges an interaction and responds with [EphemeralMessageInteractionResponseBehavior] with ephemeral flag.
 *
 * @param block [InteractionResponseCreateBuilder] used to a create an ephemeral response.
 * @return [EphemeralMessageInteractionResponseBehavior] ephemeral response to the interaction.
 */
public suspend inline fun Interaction.respondEphemeral(block: InteractionResponseCreateBuilder.() -> Unit): EphemeralMessageInteractionResponseBehavior =
    (this as ActionInteractionBehavior).respondEphemeral(block)

/**
 * Acknowledges an interaction and responds with [PublicMessageInteractionResponseBehavior].
 *
 * @param block [InteractionResponseCreateBuilder] used to create a public response.
 * @return [PublicMessageInteractionResponseBehavior] public response to the interaction.
 */
public suspend inline fun Interaction.respondPublic(block: InteractionResponseCreateBuilder.() -> Unit): PublicMessageInteractionResponseBehavior =
    (this as ActionInteractionBehavior).respondPublic(block)
