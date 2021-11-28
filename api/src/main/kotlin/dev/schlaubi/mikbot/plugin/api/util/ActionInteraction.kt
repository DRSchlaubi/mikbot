package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.interaction.Interaction
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

/**
 * Acknowledges an interaction and responds with [EphemeralInteractionResponseBehavior] with ephemeral flag.
 *
 * @param block [InteractionResponseCreateBuilder] used to a create an ephemeral response.
 * @return [InteractionResponseBehavior] ephemeral response to the interaction.
 */
public suspend inline fun Interaction.respondEphemeral(block: InteractionResponseCreateBuilder.() -> Unit): EphemeralInteractionResponseBehavior =
    (this as ActionInteractionBehavior).respondEphemeral(block)

/**
 * Acknowledges an interaction and responds with [PublicInteractionResponseBehavior].
 *
 * @param block [InteractionResponseCreateBuilder] used to create a public response.
 * @return [PublicInteractionResponseBehavior] public response to the interaction.
 */
public suspend inline fun Interaction.respondPublic(block: InteractionResponseCreateBuilder.() -> Unit): PublicInteractionResponseBehavior =
    (this as ActionInteractionBehavior).respondPublic(block)
