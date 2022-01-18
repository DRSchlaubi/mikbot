package dev.schlaubi.mikbot.game.hangman

import dev.kord.gateway.Intent
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

    override suspend fun gameSetup() {
        intents.add(Intent.GuildMessages)

        startGameCommand(
            "hangman.game.title",
            "googologo",
            { message, thread ->
                val game = HangmanGame(null, user, this@HangmanModule, message, thread, translationsProvider)
                val hostPlayer = HangmanPlayer(user)
                game.players.add(hostPlayer)

                game
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("commands.uno.leaderboard.page.title")
    }
}
