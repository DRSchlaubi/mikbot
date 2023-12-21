package dev.schlaubi.mikmusic.lyrics

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.lavakord.plugins.lyrics.rest.requestLyrics
import dev.schlaubi.lyrics.protocol.TimedLyrics
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikmusic.checks.musicQuizAntiCheat
import dev.schlaubi.mikmusic.util.musicModule

suspend fun Extension.karaokeCommand() = ephemeralSlashCommand {
    name = "karaoke"
    description = "commands.karaoke.description"

    check {
        musicQuizAntiCheat(musicModule)
    }

    action {
        val player = with(musicModule) { player }

        val lyrics = runCatching { player.requestLyrics() }.getOrNull()

        if (lyrics !is TimedLyrics) {
            discordError(translate("commands.karaoke.not_available"))
        }

        val token = requestToken(user.id)

        respond {
            content = translate("commands.karaoke.success", arrayOf("${Config.LYRICS_WEB_URL}?apiKey=$token"))
        }
    }
}
