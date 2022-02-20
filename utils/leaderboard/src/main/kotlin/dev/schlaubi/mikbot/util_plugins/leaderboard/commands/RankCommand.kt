package dev.schlaubi.mikbot.util_plugins.leaderboard.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalMember
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.util_plugins.leaderboard.*
import dev.schlaubi.mikbot.util_plugins.leaderboard.core.LeaderBoardModule

class RankArguments : Arguments() {
    val target by optionalMember {
        name = "target"
        description = "The user you want to know the rank of"
    }
}

suspend fun LeaderBoardModule.rankCommand() = publicSlashCommand(::RankArguments) {
    name = "rank"
    description = "Shows you the rank of a user"

    action {
        val target = (arguments.target ?: user).asMember(safeGuild.id)
        val profile = LeaderBoardDatabase.leaderboardEntries.findByMember(target)
        val rank = LeaderBoardDatabase.leaderboardEntries.calculateRank(safeGuild.id, profile.points)

        respond {
            embed {
                author {
                    name = target.nickname ?: target.username
                    icon = target.memberAvatar?.url ?: target.effectiveAvatar
                }

                field {
                    name = translate("commands.rank.rank")
                    value = (rank + 1).toString()
                }

                field {
                    name = translate("commands.rank.level")
                    value = profile.level.toString()
                }

                field {
                    name = translate("commands.rank.progress")
                    value = formatProgress(profile.points - calculateXpForLevel(profile.level), calculateXPForNextLevel(profile.level))
                }
            }
        }
    }
}

private fun formatProgress(current: Long, total: Long) = "█".repeat(((current.toDouble() / total.toDouble()) * 20).toInt())
    .padEnd(20, '▒') + " ${current.sanitizeNumber()}/${total.sanitizeNumber()}"

private fun Long.sanitizeNumber(): String = when (toDouble()) {
    in 1000.0..999999.0 -> "${div(1000.0)}k"
    in 1000000.0..9999999.0 -> "${div(1000000.0)}m"
    else -> toString()
}
