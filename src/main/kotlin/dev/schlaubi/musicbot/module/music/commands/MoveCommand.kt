package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.musicbot.module.music.MusicModule

class SingleSongMoveArguments : Arguments() {
    val song by int("song", "The position of the song to move")
}

class MoveArguments : Arguments() {
    val from by int("from", "The position of the song to move")
    val to by int("to", "The position to move the song to")
}

suspend fun MusicModule.moveCommand() {
    suspend fun <T : Arguments> EphemeralSlashCommand<T>.doMove(
        from: T.() -> Int,
        to: T.() -> Int,
        swap: Boolean = false,
        successMessageBuilder: suspend EphemeralSlashCommandContext<T>.(track: Track) -> String
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
        description = "Allows you to move songs in the queue"

        ephemeralSubCommand(::SingleSongMoveArguments) {
            name = "top"
            description = "Moves a Song to the top"

            doMove(SingleSongMoveArguments::song, { 0 }) { track ->
                translate("commands.move.top.success", arrayOf(track.title))
            }
        }

        ephemeralSubCommand(::MoveArguments) {
            name = "move"
            description = "Moves a Song to the the specified position"

            doMove(MoveArguments::from, MoveArguments::to) { track ->
                translate(
                    "commands.move.move.success",
                    arrayOf(
                        track.title,
                        arguments.from, arguments.to
                    )
                )
            }
        }

        ephemeralSubCommand(::MoveArguments) {
            name = "swap"
            description = "Swaps the two songs at the specified positions"

            doMove(MoveArguments::from, MoveArguments::to, swap = true) { track ->
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
            description = "Moves the last song to the top"

            doMove({ -1 }, { 0 }, swap = true) { track ->
            translate(
                "commands.move.last.success",
                arrayOf(
                    track.title,
                )
            )
        }
        }
    }
}
