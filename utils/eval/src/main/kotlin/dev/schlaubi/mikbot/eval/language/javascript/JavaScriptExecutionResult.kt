package dev.schlaubi.mikbot.eval.language.javascript

import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.eval.language.ExecutionResult

abstract class JavaScriptExecutionResult : ExecutionResult() {
    class Success(val result: Any) : ExecutionResult() {
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

    class Failing(val error: String, val stacktrace: String) : ExecutionResult() {
        override val wasSuccessful: Boolean = false

        override suspend fun EmbedBuilder.applyToEmbed() {
            field("Error") {
                "`${error}`"
            }
            if (stacktrace.isNotBlank()) {
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
