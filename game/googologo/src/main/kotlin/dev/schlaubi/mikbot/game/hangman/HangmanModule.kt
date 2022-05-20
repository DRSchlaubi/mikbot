package dev.schlaubi.mikbot.game.hangman

import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.hangman.game.HangmanGame
import dev.schlaubi.mikbot.game.hangman.game.HangmanPlayer
import org.litote.kmongo.coroutine.CoroutineCollection

class HangmanModule : GameModule<HangmanPlayer, HangmanGame>() {
    override val name: String = "googologo"
    override val bundle: String = "hangman"
    override val gameStats: CoroutineCollection<UserGameStats> = HangmanDatabase.stats

    @OptIn(PrivilegedIntent::class)
    override suspend fun gameSetup() {
        intents.add(Intent.GuildMessages)
        intents.add(Intent.MessageContent)

        startGameCommand(
            "hangman.game.title",
            "googologo",
            { message, thread ->
                HangmanGame(null, user, this@HangmanModule, message, thread, translationsProvider)
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("commands.uno.leaderboard.page.title")
    }
}
