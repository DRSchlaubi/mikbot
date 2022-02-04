package dev.schlaubi.mikbot.eval.language

import dev.kord.rest.builder.message.EmbedBuilder

sealed class TypedExecutionResult : ExecutionResult() {
    class Success(val result: Any) : TypedExecutionResult() {
        override val wasSuccessful: Boolean = true

        override suspend fun EmbedBuilder.applyToEmbed() {
            field("Result") {
                "`$result`"
            }
            field("Type") {
                "`${result::class.java.simpleName}`"
            }
        }
    }

    class Failing(val error: String, val stacktrace: String?) : TypedExecutionResult() {
        override val wasSuccessful: Boolean = false

        override suspend fun EmbedBuilder.applyToEmbed() {
            field("Error") {
                "`${error}`"
            }
            if (stacktrace?.isNotBlank() == true) {
                field("Stacktrace") {
                    """
                    |```
                    ${stacktrace.lines().joinToString(separator = "\n") { "|$it" }}
                    |```
                """.trimMargin()
                }
            }
        }
    }
}
