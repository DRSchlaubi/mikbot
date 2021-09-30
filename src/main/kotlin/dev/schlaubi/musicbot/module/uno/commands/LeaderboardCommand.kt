package dev.schlaubi.musicbot.module.uno.commands

import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.module.uno.UnoModule
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.forList
import org.litote.kmongo.eq
import org.litote.kmongo.not

@OptIn(KordUnsafe::class, KordExperimental::class)
fun UnoModule.leaderboardCommand() = publicSubCommand {
    name = "leaderboard"
    description = "Displays the best UNO players"

    action {
        val all = database.users.find(not(BotUser::unoStats eq null)).toList()

        editingPaginator {
            forList(
                user,
                all,
                {
                    val user = this@leaderboardCommand.kord.unsafe.user(it.id)
                    "${user.mention} - ${it.unoStats?.wins}/${it.unoStats?.losses} (${it.unoStats?.ratio})"
                },
                { current: Int, total: Int ->
                    translate(
                        "commands.uno.leaderboard.page.title",
                        arrayOf(current.toString(), total.toString())
                    )
                }
            )
        }.send()
    }
}
