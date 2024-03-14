package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule

suspend fun MusicModule.clearCommand() = ephemeralControlSlashCommand {
    name = "clear"
    description = "commands.clear.description"

    action {
        musicPlayer.queue.clear()
        musicPlayer.updateMusicChannelMessage()
        respond {
            content = translate("commands.clear.cleared")
        }
    }
}
