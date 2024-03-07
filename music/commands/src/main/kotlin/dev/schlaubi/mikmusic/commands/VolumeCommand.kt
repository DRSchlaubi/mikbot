package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import dev.schlaubi.mikmusic.core.MusicModule

class VolumeArguments : Arguments() {
    val volume by optionalInt {
        name = "volume"
        description = "commands.volume.arguments.volume.description"
        maxValue = 100
        minValue = 0
    }
}

suspend fun MusicModule.volumeCommand() = ephemeralControlSlashCommand(::VolumeArguments) {
    name = "volume"
    description = "commands.volume.description"

    action {
        val volume = arguments.volume
        if (volume != null) {
            val filterVolume = volume.toFloat() / 100
            musicPlayer.applyFilters {
                this.volume = filterVolume
            }

            respond { content = translate("commands.volume.set", arrayOf(volume.toString())) }
        } else {
            respond { content = translate("commands.volume.current", arrayOf(player.volume.toString())) }
        }
    }
}
