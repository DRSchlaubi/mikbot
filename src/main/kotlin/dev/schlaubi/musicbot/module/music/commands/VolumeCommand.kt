package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.lavakord.audio.player.FiltersApi
import dev.schlaubi.lavakord.audio.player.applyFilters
import dev.schlaubi.musicbot.module.music.MusicModule

class VolumeArguments : Arguments() {
    val volume by optionalInt("volume", "The desired volume", required = true) { _, value ->

        if (value !in 0..100) {
            throw DiscordRelayedException(translate("commands.volume.invalid_range"))
        }
    }
}

@OptIn(FiltersApi::class)
suspend fun MusicModule.volumeCommand() = ephemeralSlashCommand(::VolumeArguments) {
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
