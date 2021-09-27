package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.clearCommand() = ephemeralControlSlashCommand {
    name = "clear"
    description = "Clears the queue completely"

    action {
        musicPlayer.clearQueue()
        respond {
            content = translate("commands.clear.cleared")
        }
    }
}
