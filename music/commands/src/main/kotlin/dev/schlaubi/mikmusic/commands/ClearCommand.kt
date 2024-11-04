package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.clearCommand() = ephemeralControlSlashCommand {
    name = MusicTranslations.Commands.Clear.name
    description = MusicTranslations.Commands.Clear.description
    musicControlContexts()

    action {
        musicPlayer.queue.clear()
        musicPlayer.updateMusicChannelMessage()
        respond {
            content = translate(MusicTranslations.Commands.Clear.cleared)
        }
    }
}
