package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.safeGuild
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class RemoveSingleSongArguments : Arguments() {
    val position by int("position", "The position of the song to remove")
}

class RemoveRangeSongArguments : Arguments() {
    val from by int("from", "The position of the first song to remove")
    val to by int("to", "The position of the last song to remove")
}

suspend fun MusicModule.removeCommand() = ephemeralControlSlashCommand {
    name = "remove"
    description = "Removes songs from the queue"

    suspend fun <A : Arguments> EphemeralSlashCommand<A>.doRemove(
        remove: suspend EphemeralSlashCommandContext<A>.() -> Int
    ) =
        action {
            val removed = remove()
            if (removed > 0) {
                respond {
                    content = translate("commands.remove.removed", arrayOf(removed))
                }
            } else {
                respond {
                    content = translate("commands.remove.invalid_index.multiple")
                }
            }
        }

    ephemeralSubCommand(::RemoveSingleSongArguments) {
        name = "song"
        description = "Removes one song from the queue"

        action {
            val track = musicPlayer.removeQueueEntry(arguments.position - 1)
            if (track != null) {
                respond {
                    content = translate("commands.remove.song.removed", arrayOf(track.title))
                }
            } else {
                respond {
                    content = translate("commands.remove.invalid_index.single")
                }
            }
        }
    }

    ephemeralSubCommand(::RemoveRangeSongArguments) {
        name = "range"
        description = "Removes all songs from the specified range from the queue"

        doRemove {
            if (arguments.to < arguments.from) {
                throw DiscordRelayedException(translate("commands.remove.range.invalid_range_end"))
            }
            val range = arguments.from..arguments.to

            musicPlayer.removeQueueEntries(range)
        }
    }

    ephemeralSubCommand {
        name = "doubles"
        description = "Removes all dupes from the queue"

        doRemove { musicPlayer.removeDoubles() }
    }

    ephemeralSubCommand {
        name = "cleanup"
        description = "Removes songs from users which left the voice channel.\n"

        doRemove {
            val channel = musicPlayer.lastChannelId!!
            val users = safeGuild.voiceStates.filter {
                it.channelId == Snowflake(channel)
            }
                .map { it.userId }
                .toList()

            musicPlayer.removeFromUser { it !in users }
        }
    }
}
