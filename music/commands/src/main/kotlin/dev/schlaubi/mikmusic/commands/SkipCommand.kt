package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingInt
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

class SkipArguments : Arguments() {
    val to by defaultingInt {
        name = MusicTranslations.Commands.Skip.Arguments.To.name
        description = MusicTranslations.Commands.Skip.Arguments.To.description
        defaultValue = 1
    }
}

suspend fun MusicModule.skipCommand() = ephemeralControlSlashCommand(::SkipArguments) {
    name = MusicTranslations.Commands.Skip.name
    description = MusicTranslations.Commands.Skip.description
    musicControlContexts()

    action {
        if (!musicPlayer.canSkip) {
            respond { content = translate(MusicTranslations.Commands.Skip.empty) }
            return@action
        }
        if (arguments.to < 1 && musicPlayer.hasAutoPlay) {
            discordError(MusicTranslations.Commands.skips_exceed_autoplay_range)
        }
        if (arguments.to > (musicPlayer.queuedTracks.size + musicPlayer.autoPlayTrackCount)) {
            respond {
                content = translate(MusicTranslations.Commands.Skip.exceeds_range)
            }
            return@action
        }

        musicPlayer.skip(arguments.to)
        respond { content = translate(MusicTranslations.Commands.Skip.skipped) }
    }
}
