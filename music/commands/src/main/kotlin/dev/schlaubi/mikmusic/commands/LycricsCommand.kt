package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.util.fetchLyrics
import dev.schlaubi.mikmusic.util.searchHappiSong
import dev.schlaubi.stdx.core.paginate

class LyricsArguments : Arguments() {
    val name by optionalString {
        name = "song_name"
        description = "commands.lyrics.arguments.song_name.description"
    }
}

suspend fun MusicModule.lyricsCommand() = publicSlashCommand(::LyricsArguments) {
    name = "lyrics"
    description = "commands.lyrics.description"

    check {
        musicQuizAntiCheat(this@lyricsCommand)
    }

    action {
        val query = arguments.name ?: player.playingTrack?.info?.title
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
        val paged = lines.paginate(2000)

        editingPaginator {
            forList(user, paged, { it }, { _, _ -> track.track }) {
                footer {
                    text = "© ${lyrics.copyrightLabel ?: ""} | ${lyrics.copyrightNotice}"
                }
            }
        }.send()
    }
}
