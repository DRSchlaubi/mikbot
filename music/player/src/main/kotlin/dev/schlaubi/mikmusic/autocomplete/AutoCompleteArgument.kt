package dev.schlaubi.mikmusic.autocomplete

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.event.interaction.GuildAutoCompleteInteractionCreateEvent
import dev.kord.x.emoji.Emojis
import dev.schlaubi.lavakord.plugins.lavasearch.rest.search
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.mikmusic.core.Config
import dev.schlaubi.mikmusic.core.MusicModule

private val musicModule = KordExContext.get().get<ExtensibleBot>()
    .findExtensions<MusicModule>().first()

/**
 * Creates a `query` argument with [description] supporting YouTube Auto-complete.
 */
fun Arguments.autoCompletedYouTubeQuery(description: String): SingleConverter<String> = string {
    name = AUTOCOMPLETE_QUERY_OPTION
    this.description = description

    autoComplete {
        val input = focusedOption.value

        if (input.isNotBlank()) {
            val result = musicModule
                .getMusicPlayer((it as GuildAutoCompleteInteractionCreateEvent).interaction.guild)
                .search("${Config.DEFAULT_SEARCH_PROVIDER}:$input")

            suggestString {
                result.texts.take(5).map { (text) ->
                    choice(text, text)
                }
                result.artists.take(5).forEach { playlist ->
                    choice("${Emojis.man} ${playlist.info.name}", playlist.lavaSrcInfo.url)
                }
                result.tracks.take(5).forEach { track ->
                    val uri = track.info.uri ?: return@forEach
                    choice("${Emojis.notes} ${track.info.author} - ${track.info.title}", uri)
                }
                result.albums.take(5).forEach { playlist ->
                    val albumInfo = playlist.lavaSrcInfo
                    choice("${Emojis.cd} ${albumInfo.author} - ${playlist.info.name}", albumInfo.url)
                }
                result.playlists.take(5).forEach { playlist ->
                    choice("${Emojis.scroll} ${playlist.info.name}", playlist.lavaSrcInfo.url)
                }
            }
        }
    }
}
