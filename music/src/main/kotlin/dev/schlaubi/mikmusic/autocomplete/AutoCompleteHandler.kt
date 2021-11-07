package dev.schlaubi.mikmusic.autocomplete

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.interaction.respond
import dev.kord.core.behavior.interaction.respondString
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

suspend fun Extension.registerAutoCompleteHandler() = event<AutoCompleteInteractionCreateEvent> {
    action {
        val myOption = event.interaction.command.options[AUTOCOMPLETE_QUERY_OPTION]!!
        if (myOption.focused) {
            val input = myOption.value as String

            if (input.isBlank()) {
                return@action event.interaction.respond<String>(emptyList())
            } else {
                val youtubeResult = requestYouTubeAutoComplete(input)
                LOG.debug { "Responding with YouTube options: $youtubeResult" }
                event.interaction.respondString {
                    youtubeResult.take(25).forEach { choice(it, it) }
                }
            }
        }
    }
}
