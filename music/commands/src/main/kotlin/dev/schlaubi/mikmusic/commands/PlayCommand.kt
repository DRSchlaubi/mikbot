package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.queue.QueueArguments
import dev.schlaubi.mikmusic.player.queue.queueTracks

class PlayArguments : QueueArguments() {
    val search by defaultingBoolean {
        name = MusicTranslations.Commands.Play.Arguments.Search.name
        description = MusicTranslations.Commands.Play.Arguments.Search.description
        defaultValue = false
    }
}

suspend fun MusicModule.playCommand() {
    ephemeralSlashCommand(::PlayArguments) {
        musicControlContexts()

        name = MusicTranslations.Commands.Play.name
        description = MusicTranslations.Commands.Play.description

        check {
            joinSameChannelCheck(bot)
        }

        action {
            queueTracks(musicPlayer, arguments.search)
        }
    }
}
