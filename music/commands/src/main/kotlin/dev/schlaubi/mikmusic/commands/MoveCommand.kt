package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.types.respond
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikmusic.core.MusicModule

class SingleSongMoveArguments : Arguments() {
    val song by int {
        name = "song"
        description = "commands.move.arguments.song"
    }
}

class MoveArguments : Arguments() {
    val from by int {
        name = "from"
        description = "commands.move.arguments.from"
    }
    val to by int {
        name = "to"
        description = "commands.move.arguments.to"
    }
}

suspend fun MusicModule.moveCommand() {
    suspend fun <T : Arguments> EphemeralSlashCommand<T, *>.doMove(
        from: T.() -> Int,
        to: T.() -> Int,
        swap: Boolean = false,
        successMessageBuilder: suspend EphemeralSlashCommandContext<T, *>.(track: Track) -> String
    ) = action {
        @Suppress("UNCHECKED_CAST")
        val safeArguments = kotlin.runCatching { arguments }.getOrElse { Arguments() as T }
        val toValue = safeArguments.to().coerceAtLeast(1)
        if (toValue > musicPlayer.queuedTracks.size) {
            respond {
                content = translate("commands.move.invalid_index.to")
            }
            return@action
        }

        val fromRaw = safeArguments.from()
        val fromValue = if (fromRaw == -1) musicPlayer.queuedTracks.size else fromRaw.coerceAtLeast(1)

        val moved = musicPlayer.moveQueuedEntry(fromValue - 1, toValue - 1, swap)

        if (moved != null) {
            respond {
                content = successMessageBuilder(moved)
            }
        } else {
            respond {
                content = translate("commands.move.invalid_index.from")
            }
        }
    }

    ephemeralControlSlashCommand {
        name = "move"
        description = "commands.move.description"

        ephemeralSubCommand(::SingleSongMoveArguments) {
            name = "top"
            description = "commands.move.top.description"

            doMove(SingleSongMoveArguments::song, { 0 }) { track ->
                translate("commands.move.top.success", arrayOf(track.info.title))
            }
        }

        ephemeralSubCommand(::MoveArguments) {
            name = "move"
            description = "commands.move.move.description"

            doMove(MoveArguments::from, MoveArguments::to) { track ->
                translate(
                    "commands.move.move.success",
                    arrayOf(
                        track.info.title,
                        arguments.from, arguments.to
                    )
                )
            }
        }

        ephemeralSubCommand(::MoveArguments) {
            name = "swap"
            description = "commands.move.swap.description"

            doMove(MoveArguments::from, MoveArguments::to, swap = true) {
                translate(
                    "commands.move.swap.success",
                    arrayOf(
                        arguments.from, arguments.to
                    )
                )
            }
        }

        ephemeralSubCommand {
            name = "last"
            description = "commands.move.last.description"

            doMove({ -1 }, { 0 }, swap = true) { track ->
                translate(
                    "commands.move.last.success",
                    arrayOf(
                        track.info.title,
                    )
                )
            }
        }
    }
}
