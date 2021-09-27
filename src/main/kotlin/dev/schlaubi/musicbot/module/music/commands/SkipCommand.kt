package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule

class SkipArguments : Arguments() {
    val to by defaultingInt("to", "The position to skip to", 1)
}

suspend fun MusicModule.skipCommand() = ephemeralControlSlashCommand(::SkipArguments) {
    name = "skip"
    description = "Skips the current track"

    action {
        if (musicPlayer.queuedTracks.isEmpty()) {
            respond { content = translate("commands.skip.empty") }
            return@action
        }
        if (arguments.to >= musicPlayer.queuedTracks.size) {
            respond {
                content = translate("commands.skip.exceeds_range")
            }
            return@action
        }

        musicPlayer.skip(arguments.to)
        respond { content = translate("commands.skip.skipped") }
    }
}
