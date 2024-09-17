package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.replayCommand() = ephemeralControlSlashCommand {
    name = "replay"
    description = "commands.replay.description"
    musicControlContexts()

    action {
        player.seekTo(0)

        respond {
            content = translate("commands.replay.success")
        }
    }
}
