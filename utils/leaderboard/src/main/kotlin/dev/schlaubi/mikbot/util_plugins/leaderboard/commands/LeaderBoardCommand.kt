package dev.schlaubi.mikbot.util_plugins.leaderboard.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.forFlow
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.util_plugins.leaderboard.LeaderBoardDatabase
import dev.schlaubi.mikbot.util_plugins.leaderboard.core.LeaderBoardModule
import dev.schlaubi.mikbot.util_plugins.leaderboard.countLeaderboardForGuild
import dev.schlaubi.mikbot.util_plugins.leaderboard.leaderboardForGuild

suspend fun LeaderBoardModule.leaderBoardCommand() = publicSlashCommand {
    name = "Leaderboard"
    description = "Shows the current leaderboard"

    action {
        val leaderboard = LeaderBoardDatabase.leaderboardEntries.leaderboardForGuild(safeGuild.id)
        val count = LeaderBoardDatabase.leaderboardEntries.countLeaderboardForGuild(safeGuild.id)
        if (count == 0L) {
            respond {
                content = translate("command.leaderboard.empty")
            }
        } else {
            editingPaginator {
                forFlow(user, count, leaderboard, {
                    translate("commands.leaderboard.item", arrayOf("<@${it.userId}>", it.level))
                }, { current, total -> translate("commands.leaderboard.title", arrayOf(current, total)) })
            }.send()
        }
    }
}
