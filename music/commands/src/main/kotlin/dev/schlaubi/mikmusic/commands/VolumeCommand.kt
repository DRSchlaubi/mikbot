package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalInt
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

class VolumeArguments : Arguments() {
    val volume by optionalInt {
        name = MusicTranslations.Commands.Volume.Arguments.Volume.name
        description = MusicTranslations.Commands.Volume.Arguments.Volume.description
        maxValue = 1000
        minValue = 0
    }
}

suspend fun MusicModule.volumeCommand() = ephemeralControlSlashCommand(::VolumeArguments) {
    name = MusicTranslations.Commands.Volume.name
    description = MusicTranslations.Commands.Volume.description
    musicControlContexts()

    action {
        val volume = arguments.volume
        if (volume != null) {
            musicPlayer.changeVolume(volume)
            respond { content = translate(MusicTranslations.Commands.Volume.set, volume.toString()) }
        } else {
            respond { content = translate(MusicTranslations.Commands.Volume.current, player.volume.toString()) }
        }
    }
}
