package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.KSoftUtil
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.splitIntoPages

class LyricsArguments : Arguments() {
    val name by optionalString("song_name", "The name of the song to search for, if no one is playing")
}

suspend fun MusicModule.lyricsCommand() = publicSlashCommand(::LyricsArguments) {

    action {
        val query = arguments.name ?: player.playingTrack?.let {
            it.title + " - " + it.author
        }

        if (query == null) {
            respond {
                content = translate("command.lyrics.no_song_playing")
            }

            return@action
        }
        val lyrics = KSoftUtil.searchForLyric(query)
        if (lyrics == null) {
            respond {
                content = translate("command.lyrics.no_lyrics")
            }

            return@action
        }

        val lines = lyrics.lyrics.lines()
        val paged = lines.splitIntoPages(2000)

        editingPaginator {
            forList(user, paged, { it }, { _, _ -> lyrics.name }) {
                footer {
                    text = "Source: KSoft"
                }
            }
        }
    }
}
