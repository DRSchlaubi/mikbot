package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule

class SkipArguments : Arguments() {
    val to by defaultingInt {
        name = "to"
        description = "commands.skip.arguments.to.description"
        defaultValue = 1
    }
}

suspend fun MusicModule.skipCommand() = ephemeralControlSlashCommand(::SkipArguments) {
    name = "skip"
    description = "commands.skip.description"

    action {
        if (!musicPlayer.canSkip) {
            respond { content = translate("commands.skip.empty") }
            return@action
        }
        if (arguments.to > musicPlayer.queuedTracks.size) {
            respond {
                content = translate("commands.skip.exceeds_range")
            }
            return@action
        }

        musicPlayer.skip(arguments.to)
        respond { content = translate("commands.skip.skipped") }
    }
}
