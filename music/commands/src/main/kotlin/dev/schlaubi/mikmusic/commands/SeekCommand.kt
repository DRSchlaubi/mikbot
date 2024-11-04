package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val seekRegex = "([0-9]+)(?::([0-9]+))?".toRegex()

class SeekArguments : Arguments() {
    val to by string {
        name = MusicTranslations.Commands.Seek.Arguments.To.name
        description = MusicTranslations.Commands.Seek.Arguments.To.description

        validate {
            if (!value.matches(seekRegex)) {
                discordError(MusicTranslations.Command.Seek.invalid_format)
            }
        }
    }
}

suspend fun MusicModule.seekCommand() = ephemeralSlashCommand(::SeekArguments) {
    name = MusicTranslations.Commands.Seek.name
    description = MusicTranslations.Commands.Seek.description
    musicControlContexts()

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
            content = translate(MusicTranslations.Commands.Seek.success, position)
        }
    }
}
