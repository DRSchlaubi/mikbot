package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val seekRegex = "([0-9]+)(?::([0-9]+))?".toRegex()

class SeekArguments : Arguments() {
    val to by string("to", "The position to skip to (mm:ss)") { _, value ->
        if (!value.matches(seekRegex)) {
            throw DiscordRelayedException(translate("command.seek.invalid_format"))
        }
    }
}

suspend fun MusicModule.seekCommand() = ephemeralSlashCommand(::SeekArguments) {
    name = "seek"
    description = "Seeks to a specified position in the track"

    check {
        anyMusicPlaying(this@seekCommand)
    }

    action {
        val (minutes, seconds) = seekRegex.find(arguments.to)!!.destructured

        val position =
            minutes.toInt().minutes + if (seconds.isEmpty()) {
                0.seconds
            } else {
                seconds.toInt().seconds
            }

        player.seekTo(position)

        respond {
            content = translate("commands.seek.success", arrayOf(position.toString()))
        }
    }
}
