package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

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
    musicControlContexts()

    action {
        if (!musicPlayer.canSkip) {
            respond { content = translate("commands.skip.empty") }
            return@action
        }
        if (arguments.to < 1 && musicPlayer.hasAutoPlay) {
            discordError(translate("commands.skips_exceed_autoplay_range"))
        }
        if (arguments.to > (musicPlayer.queuedTracks.size + musicPlayer.autoPlayTrackCount)) {
            respond {
                content = translate("commands.skip.exceeds_range")
            }
            return@action
        }

        musicPlayer.skip(arguments.to)
        respond { content = translate("commands.skip.skipped") }
    }
}
