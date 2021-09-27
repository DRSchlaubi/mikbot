package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.skipCommand() = ephemeralSlashCommand {
    name = "skip"
    description = "Skips the current track"

    action {
        if (musicPlayer.queuedTracks.isEmpty()) {
            respond { content = translate("commands.skip.empty") }
            return@action
        }

        musicPlayer.skip()
        respond { content = translate("commands.skip.skipped") }
    }
}
