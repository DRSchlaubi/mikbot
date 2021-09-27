package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.replayCommand() = ephemeralControlSlashCommand {
    name = "replay"
    description = "Replays the current song"

    action {
        player.seekTo(0)

        respond {
            content = translate("commands.replay.success")
        }
    }
}
