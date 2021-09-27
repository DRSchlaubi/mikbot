package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.pauseCommand() = ephemeralControlSlashCommand {
    name = "pause"
    description = "Toggles the playback"

    action {
        player.pause(!link.player.paused)

        respond { content = "Pause toggle" }
    }
}
