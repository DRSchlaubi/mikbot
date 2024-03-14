package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.MusicModule
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class RemoveSingleSongArguments : Arguments() {
    val position by int {
        name = "position"
        description = "commands.remove.arguments.position.description"
    }
}

class RemoveRangeSongArguments : Arguments() {
    val from by int {
        name = "from"
        description = "commands.remove.arguments.from.description"
    }
    val to by int {
        name = "to"
        description = "commands.remove.arguments.to.description"
    }
}

suspend fun MusicModule.removeCommand() = ephemeralControlSlashCommand {
    name = "remove"
    description = "commands.remove.description"

    suspend fun <A : Arguments> EphemeralSlashCommand<A, *>.doRemove(
        remove: suspend EphemeralSlashCommandContext<A, *>.() -> Int
    ) =
        action {
            val removed = remove()
            musicPlayer.updateMusicChannelMessage()
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
        description = "commands.remove.song.description"

        action {
            val track = musicPlayer.queue.removeQueueEntry(arguments.position - 1)
            musicPlayer.updateMusicChannelMessage()
            if (track != null) {
                respond {
                    content = translate("commands.remove.song.removed", arrayOf(track.info.title))
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
        description = "commands.remove.range.description"

        doRemove {
            if (arguments.to < arguments.from) {
                throw DiscordRelayedException(translate("commands.remove.range.invalid_range_end"))
            }
            val range = arguments.from..arguments.to

            musicPlayer.queue.removeQueueEntries(range)
        }
    }

    ephemeralSubCommand {
        name = "doubles"
        description = "commands.remove.doubles.description"

        doRemove { musicPlayer.queue.removeDoubles() }
    }

    ephemeralSubCommand {
        name = "cleanup"
        description = "commands.remove.cleanup.description"

        doRemove {
            val channel = musicPlayer.lastChannelId!!
            val users = safeGuild.voiceStates.filter {
                it.channelId == Snowflake(channel)
            }
                .map { it.userId }
                .toList()

            musicPlayer.queue.removeFromUser { it !in users }
        }
    }
}
