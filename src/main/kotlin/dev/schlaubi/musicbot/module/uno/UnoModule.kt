package dev.schlaubi.musicbot.module.uno

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.musicbot.game.GameStats
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.game.module.commands.leaderboardCommand
import dev.schlaubi.musicbot.game.module.commands.profileCommand
import dev.schlaubi.musicbot.game.module.commands.startGameCommand
import dev.schlaubi.musicbot.game.module.commands.stopGameCommand
import dev.schlaubi.musicbot.module.settings.BotUser
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.musicbot.module.uno.game.player.DiscordUnoPlayer
import kotlin.reflect.KProperty1

class UnoModule : GameModule<DiscordUnoPlayer, DiscordUnoGame>() {
    override val name: String = "uno"
    override val bundle: String = "uno"
    override val commandName: String = "uno"
    override val gameStats: KProperty1<BotUser, GameStats?> = BotUser::unoStats

    @OptIn(PrivilegedIntent::class, KordUnsafe::class, KordExperimental::class)
    override suspend fun gameSetup() {
        intents.add(Intent.GuildMembers)

        startGameCommand(
            "uno.game.title",
            "uno-game"
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("commands.uno.leaderboard.page.title")
    }

    override fun obtainGame(
        host: UserBehavior,
        welcomeMessage: Message,
        thread: ThreadChannelBehavior,
        translationsProvider: TranslationsProvider
    ): DiscordUnoGame = DiscordUnoGame(host, this, welcomeMessage, thread, translationsProvider)
}
