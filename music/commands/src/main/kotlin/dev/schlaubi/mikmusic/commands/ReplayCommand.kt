package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.replayCommand() = ephemeralControlSlashCommand {
    name = MusicTranslations.Commands.Replay.name
    description = MusicTranslations.Commands.Replay.description
    musicControlContexts()

    action {
        player.seekTo(0)

        respond {
            content = translate(MusicTranslations.Commands.Replay.success)
        }
    }
}
