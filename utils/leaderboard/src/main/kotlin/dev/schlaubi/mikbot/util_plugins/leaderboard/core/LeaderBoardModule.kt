package dev.schlaubi.mikbot.util_plugins.leaderboard.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.mikbot.util_plugins.leaderboard.commands.leaderBoardCommand
import dev.schlaubi.mikbot.util_plugins.leaderboard.commands.rankCommand

class LeaderBoardModule : Extension() {
    override val name: String = "leaderboard"
    override val bundle: String = "leaderboard"
    override val allowApplicationCommandInDMs: Boolean = false

    @OptIn(PrivilegedIntent::class)
    override suspend fun setup() {
        intents.add(Intent.GuildMembers)
        intents.add(Intent.GuildPresences)

        leaderBoardExecutor()
        rankCommand()
        leaderBoardCommand()
    }
}
