package dev.schlaubi.mikbot.eval.language

import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.eval.secrets.SecretExtensionPoint
import dev.schlaubi.mikbot.haste.HasteClient
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val extensions by lazy {
    pluginSystem.getExtensions<SecretExtensionPoint>()
}

sealed class TypedExecutionResult : ExecutionResult() {
    class Success(result: Any, val type: String) : TypedExecutionResult(), KoinComponent {
        override val wasSuccessful: Boolean = true

        private val hasteClient by inject<HasteClient>()

        private val result =
            extensions.asSequence()
                .flatMap(SecretExtensionPoint::provideSecrets)
                .filter(String::isNotBlank)
                .fold(result.toString()) { acc, secret ->
                    acc.replace(secret, "**REDACTED**")
                }

        override suspend fun EmbedBuilder.applyToEmbed() {
            field("Result") {
                if (result.length > 1024) {
                    // Too long to post into discord. We create a haste for that.
                    val haste = hasteClient.createHaste(result)
                    "Result was too long to show.\nView it [here](${haste.url})."
                } else {
                    "`$result`"
                }
            }
            field("Type") {
                "`$type`"
            }
        }
    }

    class Failing(val error: String, val stacktrace: String?) : TypedExecutionResult() {
        override val wasSuccessful: Boolean = false

        override suspend fun EmbedBuilder.applyToEmbed() {
            field("Error") {
                "`$error`"
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
