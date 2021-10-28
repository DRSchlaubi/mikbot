package dev.schlaubi.musicbot.module.music.autocomplete

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

/**
 * Creates a `query` argument with [description] supporting YouTube Auto-complete.
 */
fun Arguments.autoCompletedYouTubeQuery(description: String): SingleConverter<String> {
    return arg(
        displayName = AUTOCOMPLETE_QUERY_OPTION,
        description = description,

        converter = AutoCompletedStringConverter()
    )
}
