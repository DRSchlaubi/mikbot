package dev.schlaubi.mikbot.game.connect_four

import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object Connect4Database : KoinComponent {
    val stats = database.getCollection<UserGameStats>("connect_four_stats")
}
