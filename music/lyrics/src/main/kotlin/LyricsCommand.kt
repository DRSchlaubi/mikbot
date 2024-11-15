package dev.schlaubi.mikmusic.lyrics

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.schlaubi.lavakord.RestException
import dev.schlaubi.lavakord.plugins.lyrics.rest.requestLyrics
import dev.schlaubi.lavakord.plugins.lyrics.rest.searchLyrics
import dev.schlaubi.lyrics.protocol.TimedLyrics
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.LyricsTranslations
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.util.musicModule
import dev.schlaubi.stdx.core.paginate

class LyricsArguments : Arguments() {
    val name by optionalString {
        name = LyricsTranslations.Commands.Lyrics.Arguments.SongName.name
        description = LyricsTranslations.Commands.Lyrics.Arguments.SongName.description
    }
}

suspend fun Extension.lyricsCommand() = publicSlashCommand(::LyricsArguments) {
    name = LyricsTranslations.Commands.Lyrics.name
    description = LyricsTranslations.Commands.Lyrics.description

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
                content = translate(LyricsTranslations.Command.Lyrics.noSongPlaying)
            }

            return@action
        }
        val lyrics = try {
            if (arguments.name == null && player.playingTrack != null) {
                player.requestLyrics()
            } else {
                val (videoId) = link.node.searchLyrics(query).firstOrNull()
                    ?: discordError(LyricsTranslations.Command.Lyrics.noLyrics)
                link.node.requestLyrics(videoId)
            }
        } catch (e: RestException) {
            discordError(LyricsTranslations.Command.Lyrics.noLyrics)
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
                    text = translate(LyricsTranslations.Command.Lyrics.source, lyrics.source)
                }
            }
        }.send()
    }
}
