package dev.schlaubi.mikbot.game.tic_tac_toe

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object TicTacToeDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("tic_tac_toe_stats")
}
