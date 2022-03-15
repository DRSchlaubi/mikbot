package dev.schlaubi.mikbot.eval.language

import dev.kord.rest.builder.message.EmbedBuilder

abstract class ExecutionResult {
    abstract val wasSuccessful: Boolean
    abstract suspend fun EmbedBuilder.applyToEmbed()
}
