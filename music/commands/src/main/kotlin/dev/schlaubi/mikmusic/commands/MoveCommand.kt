package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.EphemeralSlashCommand
import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.int
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.toKey
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

class SingleSongMoveArguments : Arguments() {
    val song by int {
        name = MusicTranslations.Commands.Move.Arguments.Song.name
        description = MusicTranslations.Commands.Move.Arguments.Song.description
    }
}

class MoveArguments : Arguments() {
    val from by int {
        name = MusicTranslations.Commands.Move.Arguments.From.name
        description = MusicTranslations.Commands.Move.Arguments.From.description
    }
    val to by int {
        name = MusicTranslations.Commands.Move.Arguments.To.name
        description = MusicTranslations.Commands.Move.Arguments.To.description
    }
}

suspend fun MusicModule.moveCommand() {
    suspend fun <T : Arguments> EphemeralSlashCommand<T, *>.doMove(
        from: T.() -> Int,
        to: T.() -> Int,
        swap: Boolean = false,
        successMessageBuilder: suspend EphemeralSlashCommandContext<T, *>.(track: Track) -> String,
    ) = action {
        @Suppress("UNCHECKED_CAST")
        val safeArguments = kotlin.runCatching { arguments }.getOrElse { Arguments() as T }
        val toValue = safeArguments.to().coerceAtLeast(1)
        if (toValue > musicPlayer.queuedTracks.size) {
            respond {
                content = translate(MusicTranslations.Commands.Move.Invalid_index.to)
            }
            return@action
        }

        val fromRaw = safeArguments.from()
        val fromValue = if (fromRaw == -1) musicPlayer.queuedTracks.size else fromRaw.coerceAtLeast(1)

        val moved = musicPlayer.queue.moveQueuedEntry(fromValue - 1, toValue - 1, swap)
        musicPlayer.updateMusicChannelMessage()

        if (moved != null) {
            respond {
                content = successMessageBuilder(moved)
            }
        } else {
            respond {
                content = translate(MusicTranslations.Commands.Move.Invalid_index.from)
            }
        }
    }

    ephemeralControlSlashCommand {
        name = MusicTranslations.Commands.Move.name
        description = "<not used>".toKey()
        musicControlContexts()

        ephemeralSubCommand(::SingleSongMoveArguments) {
            name = MusicTranslations.Commands.Move.Top.name
            description = MusicTranslations.Commands.Move.Top.description

            doMove(SingleSongMoveArguments::song, { 0 }) { track ->
                translate(MusicTranslations.Commands.Move.Top.success, track.info.title)
            }
        }

        ephemeralSubCommand(::MoveArguments) {
            name = MusicTranslations.Commands.Move.Move.name
            description = MusicTranslations.Commands.Move.Move.description

            doMove(MoveArguments::from, MoveArguments::to) { track ->
                translate(
                    MusicTranslations.Commands.Move.Move.success,
                    track.info.title,
                    arguments.from, arguments.to
                )
            }
        }

        ephemeralSubCommand(::MoveArguments) {
            name = MusicTranslations.Commands.Move.Swap.name
            description = MusicTranslations.Commands.Move.Swap.description

            doMove(MoveArguments::from, MoveArguments::to, swap = true) {
                translate(MusicTranslations.Commands.Move.Swap.success, it.info.title)
            }
        }

        ephemeralSubCommand {
            name = MusicTranslations.Commands.Move.Last.name
            description = MusicTranslations.Commands.Move.Last.description

            doMove({ -1 }, { 0 }, swap = true) { track ->
                translate(MusicTranslations.Commands.Move.Last.success, track.info.title)
            }
        }
    }
}
