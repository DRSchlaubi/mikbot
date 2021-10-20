package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.musicQuizAntiCheat
import dev.schlaubi.musicbot.utils.fetchLyrics
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.searchHappiSong
import dev.schlaubi.musicbot.utils.splitIntoPages

class LyricsArguments : Arguments() {
    val name by optionalString("song_name", "The name of the song to search for, if no one is playing")
}

suspend fun MusicModule.lyricsCommand() = publicSlashCommand(::LyricsArguments) {
    name = "lyrics"
    description = "Displays the lyrics for the current song or the specified query"

    check {
        musicQuizAntiCheat(this@lyricsCommand)
    }

    action {
        val query = arguments.name ?: player.playingTrack?.title
            ?.replace("\\(.+(?!\\))".toRegex(), "") // replace anything in brackets like (official music video)

        if (query == null) {
            respond {
                content = translate("command.lyrics.no_song_playing")
            }

            return@action
        }
        val trackResponse = searchHappiSong(query)
        if (trackResponse.length != 1) {
            respond {
                content = translate("command.lyrics.no_lyrics")
            }
            return@action
        }
        val track = trackResponse.result!!.first()
        val lyrics = track.fetchLyrics().result!!

        val lines = lyrics.lyrics.lines()
        val paged = lines.splitIntoPages(2000)

        editingPaginator {
            forList(user, paged, { it }, { _, _ -> track.track }) {
                footer {
                    text = "© ${lyrics.copyrightLabel} | ${lyrics.copyrightNotice}"
                }
            }
        }.send()
    }
}
