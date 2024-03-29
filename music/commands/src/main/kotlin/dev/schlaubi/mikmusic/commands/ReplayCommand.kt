package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule

suspend fun MusicModule.replayCommand() = ephemeralControlSlashCommand {
    name = "replay"
    description = "commands.replay.description"

    action {
        player.seekTo(0)

        respond {
            content = translate("commands.replay.success")
        }
    }
}
