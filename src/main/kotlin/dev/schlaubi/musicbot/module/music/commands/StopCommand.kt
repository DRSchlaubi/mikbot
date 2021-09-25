package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.stopCommand() {
    ephemeralSlashCommand {
        name = "die"
        description = "Stops the current song"

        action {
            musicPlayer.stop()

            respond { content = "Stopped playback" }
        }
    }
}
