package dev.schlaubi.musicbot.game.module.commands

import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.forFlow
import dev.schlaubi.musicbot.utils.safeGuild
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
        val filter = not(gameStats eq null)
        val count = database.users.countDocuments(filter)
        val all = database.users
            .find(filter)
            .sort(descending(gameStats / GameStats::totalGamesPlayed, gameStats / GameStats::ratio))
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
