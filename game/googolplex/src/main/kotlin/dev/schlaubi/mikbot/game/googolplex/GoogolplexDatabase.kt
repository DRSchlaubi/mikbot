package dev.schlaubi.mikbot.game.googolplex

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object GoogolplexDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("googolplex_stats")
}
