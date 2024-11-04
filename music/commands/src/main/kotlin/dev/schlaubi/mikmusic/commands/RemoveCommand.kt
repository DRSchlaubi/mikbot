package dev.schlaubi.mikmusic.commands

import dev.kord.common.entity.Snowflake
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.EphemeralSlashCommand
import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.int
import dev.kordex.core.commands.converters.impl.optionalInt
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class RemoveSingleSongArguments : Arguments() {
    val position by int {
        name = MusicTranslations.Commands.Remove.Arguments.Position.name
        description = MusicTranslations.Commands.Remove.Arguments.Position.description
    }
}

class RemoveRangeSongArguments : Arguments() {
    val from by int {
        name = MusicTranslations.Commands.Remove.Arguments.From.name
        description = MusicTranslations.Commands.Remove.Arguments.From.description
    }
    val to by optionalInt {
        name = MusicTranslations.Commands.Remove.Arguments.To.name
        description = MusicTranslations.Commands.Remove.Arguments.To.description
    }
}

suspend fun MusicModule.removeCommand() = ephemeralControlSlashCommand {
    name = MusicTranslations.Commands.Remove.name
    description = MusicTranslations.Commands.Remove.description
    musicControlContexts()

    suspend fun <A : Arguments> EphemeralSlashCommand<A, *>.doRemove(
        remove: suspend EphemeralSlashCommandContext<A, *>.() -> Int,
    ) =
        action {
            val removed = remove()
            musicPlayer.updateMusicChannelMessage()
            if (removed > 0) {
                respond {
                    content = translate(MusicTranslations.Commands.Remove.removed, removed)
                }
            } else {
                respond {
                    content = translate(MusicTranslations.Commands.Remove.InvalidIndex.multiple)
                }
            }
        }

    ephemeralSubCommand(::RemoveSingleSongArguments) {
        name = MusicTranslations.Commands.Remove.Song.name
        description = MusicTranslations.Commands.Remove.Song.description

        action {
            val track = musicPlayer.queue.removeQueueEntry(arguments.position - 1)
            musicPlayer.updateMusicChannelMessage()
            if (track != null) {
                respond {
                    content = translate(MusicTranslations.Commands.Remove.Song.removed, track.info.title)
                }
            } else {
                respond {
                    content = translate(MusicTranslations.Commands.Remove.InvalidIndex.single)
                }
            }
        }
    }

    ephemeralSubCommand(::RemoveRangeSongArguments) {
        name = MusicTranslations.Commands.Remove.Range.name
        description = MusicTranslations.Commands.Remove.Range.description

        doRemove {
            val to = arguments.to ?: arguments.from
            if (to < arguments.from) {
                discordError(MusicTranslations.Commands.Remove.Range.invalidRangeEnd)
            }
            val range = arguments.from..to

            musicPlayer.queue.removeQueueEntries(range)
        }
    }

    ephemeralSubCommand {
        name = MusicTranslations.Commands.Remove.Doubles.name
        description = MusicTranslations.Commands.Remove.Doubles.description

        doRemove { musicPlayer.queue.removeDoubles() }
    }

    ephemeralSubCommand {
        name = MusicTranslations.Commands.Remove.Cleanup.name
        description = MusicTranslations.Commands.Remove.Cleanup.description

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
