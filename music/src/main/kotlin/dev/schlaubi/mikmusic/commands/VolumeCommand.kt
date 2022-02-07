package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule

class VolumeArguments : Arguments() {
    val volume by optionalInt {
        name = "volume"
        description = "The desired volume"

        validate {
            if (value !in 0..100) {
                throw DiscordRelayedException(translate("commands.volume.invalid_range"))
            }
        }
    }
}

suspend fun MusicModule.volumeCommand() = ephemeralControlSlashCommand(::VolumeArguments) {
    name = "volume"
    description = "Changes the volume of the bot"

    action {
        val volume = arguments.volume
        if (volume != null) {
            val filterVolume = volume.toFloat() / 100
            musicPlayer.applyFilters {
                this.volume = filterVolume
            }

            respond { content = translate("commands.volume.set", arrayOf(volume.toString())) }
        } else {
            respond { content = translate("commands.volume.current", arrayOf((player.volume / 10).toString())) }
        }
    }
}
