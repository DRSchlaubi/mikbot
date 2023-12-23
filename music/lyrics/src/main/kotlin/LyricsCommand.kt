package dev.schlaubi.mikmusic.lyrics

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import dev.schlaubi.lavakord.RestException
import dev.schlaubi.lavakord.plugins.lyrics.rest.requestLyrics
import dev.schlaubi.lavakord.plugins.lyrics.rest.searchLyrics
import dev.schlaubi.lyrics.protocol.TimedLyrics
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.util.musicModule
import dev.schlaubi.stdx.core.paginate

class LyricsArguments : Arguments() {
    val name by optionalString {
        name = "song_name"
        description = "commands.lyrics.arguments.song_name.description"
    }
}

suspend fun Extension.lyricsCommand() = publicSlashCommand(::LyricsArguments) {
    name = "lyrics"
    description = "commands.lyrics.description"

    check {
        musicQuizAntiCheat(musicModule)
    }

    action {
        val player = with(musicModule) { player }
        val link = with(musicModule) { link }

        val query = arguments.name ?: player.playingTrack?.info?.title
            ?.replace("\\(.+(?!\\))".toRegex(), "") // replace anything in brackets like (official music video)

        if (query == null) {
            respond {
                content = translate("command.lyrics.no_song_playing")
            }

            return@action
        }
        val lyrics = try {
            if (arguments.name != null && player.playingTrack != null) {
                player.requestLyrics()
            } else {
                val (videoId) = link.node.searchLyrics(query).firstOrNull()
                    ?: discordError(translate("command.lyrics.no_lyrics"))
                link.node.requestLyrics(videoId)
            }
        } catch (e: RestException) {
            discordError(translate("command.lyrics.no_lyrics"))
        }

        val lines = if (lyrics is TimedLyrics) {
            lyrics.lines.map {
                if (player.position in it.range) {
                    "**__${it.line}__**"
                } else {
                    it.line
                }
            }
        } else {
            lyrics.text.lines()
        }
        val paged = lines.paginate(2000)

        editingPaginator {
            forList(user, paged, { it }, { _, _ -> lyrics.track.title }) {
                footer {
                    text = translate("command.lyrics.source", arrayOf(lyrics.source))
                }
            }
        }.send()
    }
}
