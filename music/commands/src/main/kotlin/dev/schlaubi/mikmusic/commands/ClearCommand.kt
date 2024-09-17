package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.clearCommand() = ephemeralControlSlashCommand {
    name = "clear"
    description = "commands.clear.description"
    musicControlContexts()

    action {
        musicPlayer.queue.clear()
        musicPlayer.updateMusicChannelMessage()
        respond {
            content = translate("commands.clear.cleared")
        }
    }
}
