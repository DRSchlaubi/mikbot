package dev.schlaubi.mikmusic.lyrics

import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.lavakord.plugins.lyrics.rest.requestLyrics
import dev.schlaubi.lyrics.protocol.TimedLyrics
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.LyricsTranslations
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.util.musicModule

suspend fun Extension.karaokeCommand() = ephemeralSlashCommand {
    name = LyricsTranslations.Commands.Karaoke.name
    description = LyricsTranslations.Commands.Karaoke.description

    check {
        musicQuizAntiCheat(musicModule)
    }

    action {
        val player = with(musicModule) { player }

        val lyrics = runCatching { player.requestLyrics() }.getOrNull()

        if (lyrics !is TimedLyrics) {
            discordError(LyricsTranslations.Commands.Karaoke.notAvailable)
        }

        val token = requestToken(user.id)

        respond {
            content = translate(LyricsTranslations.Commands.Karaoke.success, "${Config.LYRICS_WEB_URL}?apiKey=$token")
        }
    }
}
