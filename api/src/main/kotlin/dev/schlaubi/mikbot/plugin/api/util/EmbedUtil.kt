package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.rest.builder.message.EmbedBuilder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates an [EmbedBuilder] and applies [builder] to it.
 */
@OptIn(ExperimentalContracts::class)
public inline fun embed(builder: EmbedBuilder.() -> Unit): EmbedBuilder {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return EmbedBuilder().also(builder)
}
