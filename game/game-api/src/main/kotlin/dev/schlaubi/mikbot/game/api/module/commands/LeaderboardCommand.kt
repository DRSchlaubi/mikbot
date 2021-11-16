package dev.schlaubi.mikbot.game.api.module.commands

import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.schlaubi.mikbot.game.api.GameStats
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.plugin.api.util.forFlow
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import org.litote.kmongo.descending
import org.litote.kmongo.div

/**
 * Adds a /leaderboard command to this [GameModule].
 * @param leaderboardTitleKey the translation key for the embed title
 */
@OptIn(KordUnsafe::class, KordExperimental::class)
fun GameModule<*, *>.leaderboardCommand(
    leaderboardTitleKey: String
) = publicSubCommand {
    name = "leaderboard"
    description = "Displays the best players"

    action {
        val count = gameStats.countDocuments()
        val all = gameStats.find()
            .sort(
                descending(
                    UserGameStats::stats / GameStats::totalGamesPlayed,
                    UserGameStats::stats / GameStats::ratio
                )
            )
            .toFlow()

        editingPaginator {
            forFlow(
                user,
                count,
                all,
                { (userId, stats) ->
                    val ratio = stats.ratio.formatPercentage()
                    val user = user.kord.unsafe.user(userId)

                    "${
                        user.asMemberOrNull(safeGuild.id)?.mention ?: user.asUserOrNull()?.username
                        ?: user.mention
                    } - ${stats.wins}/${stats.losses} ($ratio)"
                },
                { current: Int, total: Int ->
                    translate(
                        leaderboardTitleKey,
                        arrayOf(current.toString(), total.toString())
                    )
                }
            )
        }.send()
    }
}
