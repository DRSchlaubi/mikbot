package dev.schlaubi.mikbot.game.uno

import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object UnoDatabase : KoinComponent {
    val stats = database.getCollection<UserGameStats>("uno_stats")
}
