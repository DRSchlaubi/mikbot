package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.anyMusicPlaying
import kotlin.time.Duration

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
            Duration.minutes(minutes.toInt()) + if (seconds.isEmpty()) {
                Duration.seconds(0)
            } else {
                Duration.seconds(seconds.toInt())
            }

        player.seekTo(position)

        respond {
            content = translate("commands.seek.success", arrayOf(position.toString()))
        }
    }
}
