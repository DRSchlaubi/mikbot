package dev.schlaubi.mikbot.game.tic_tac_toe

import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object TicTacToeDatabase : KoinComponent {
    val stats = database.getCollection<UserGameStats>("tic_tac_toe_stats")
}
