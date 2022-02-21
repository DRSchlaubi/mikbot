package dev.schlaubi.mikbot.util_plugins.leaderboard.core

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.mikbot.util_plugins.leaderboard.commands.leaderBoardCommand
import dev.schlaubi.mikbot.util_plugins.leaderboard.commands.rankCommand

class LeaderBoardModule : Extension() {
    override val name: String = "leaderboard"
    override val bundle: String = "leaderboard"

    init {
        slashCommandCheck {
            anyGuild()
        }
    }

    @OptIn(PrivilegedIntent::class)
    override suspend fun setup() {
        intents.add(Intent.GuildMembers)
        intents.add(Intent.GuildPresences)

        leaderBoardExecutor()
        rankCommand()
        leaderBoardCommand()
    }
}
