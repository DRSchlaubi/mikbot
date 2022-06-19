package dev.schlaubi.mikbot.game.connect_four

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object Connect4Database : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("connect_four_stats")
}
