package dev.schlaubi.mikmusic.autocomplete

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import dev.kord.common.Locale
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.mikmusic.innerttube.requestYouTubeAutoComplete

/**
 * Creates a `query` argument with [description] supporting YouTube Auto-complete.
 */
fun Arguments.autoCompletedYouTubeQuery(description: String): SingleConverter<String> = string {
    name = AUTOCOMPLETE_QUERY_OPTION
    this.description = description

    autoComplete {
        val input = focusedOption.value

        if (input.isNotBlank()) {
            val youtubeResult = requestYouTubeAutoComplete(input, locale ?: Locale.ENGLISH_UNITED_STATES)
            suggestString {
                youtubeResult.take(25).forEach { choice(it, it) }
            }
        }
    }
}
