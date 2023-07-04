package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule

suspend fun MusicModule.clearCommand() = ephemeralControlSlashCommand {
    name = "clear"
    description = "commands.clear.description"

    action {
        musicPlayer.clearQueue()
        respond {
            content = translate("commands.clear.cleared")
        }
    }
}
