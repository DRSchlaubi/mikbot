package dev.schlaubi.musicbot.module.uno

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.kord.core.entity.channel.TextChannel
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.musicbot.module.SubCommandModule
import dev.schlaubi.musicbot.module.uno.commands.leaderboardCommand
import dev.schlaubi.musicbot.module.uno.commands.profileCommand
import dev.schlaubi.musicbot.module.uno.commands.startGameCommand
import dev.schlaubi.musicbot.module.uno.commands.stopGameCommand
import kotlinx.coroutines.runBlocking

class UnoModule : SubCommandModule() {
    override val name: String = "uno"
    override val bundle: String = "uno"
    override val commandName: String = "uno"
    override val commandDescription: String =
        "Command for the Bots built-in UNO game feature, and yes all music bots should have an uno feature"

    val ApplicationCommandContext.textChannel: TextChannel
        get() = runBlocking { channel.asChannel() as TextChannel }

    @OptIn(PrivilegedIntent::class)
    override suspend fun overrideSetup() {
        intents.add(Intent.GuildMembers)

        slashCommandCheck { anyGuild() }

        startGameCommand()
        stopGameCommand()
        profileCommand()
        leaderboardCommand()
    }
}
