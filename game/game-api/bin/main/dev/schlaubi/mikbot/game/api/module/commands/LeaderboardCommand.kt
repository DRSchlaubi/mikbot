package dev.schlaubi.mikbot.game.api.module.commands

import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.schlaubi.mikbot.game.api.module.GameModule
import org.litote.kmongo.descending
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.not

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
        val count = gameStats.countDocuments(filter)
        val all = gameStats.find()
            .sort(
                descending(
                    UserGameStats::stats / totalGamesPlayed,
                    UserGameStats::stats / GameStats::ratio
                )
            )
            .toFlow()

        editingPaginator {
            forFlow(
                user,
                count,
                all,
                {
                    val user = this@leaderboardCommand.kord.unsafe.user(it.id)
                    val stats = gameStats.get(it)!!
                    val ratio = stats.ratio.formatPercentage()
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
