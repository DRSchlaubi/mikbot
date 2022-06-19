package dev.schlaubi.mikbot.game.uno

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object UnoDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("uno_stats")
}
