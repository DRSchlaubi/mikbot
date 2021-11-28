package dev.schlaubi.mikmusic.autocomplete

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

suspend fun Extension.registerAutoCompleteHandler() = event<AutoCompleteInteractionCreateEvent> {
    action {
        val interaction = event.interaction as AutoCompleteInteraction
        val myOption = interaction.command.options[AUTOCOMPLETE_QUERY_OPTION]!!
        if (myOption.focused) {
            val input = myOption.value as String

            if (input.isBlank()) {
                return@action interaction.suggest<String>(emptyList())
            } else {
                val youtubeResult = requestYouTubeAutoComplete(input)
                LOG.debug { "Responding with YouTube options: $youtubeResult" }
                interaction.suggestString {
                    youtubeResult.take(25).forEach { choice(it, it) }
                }
            }
        }
    }
}
