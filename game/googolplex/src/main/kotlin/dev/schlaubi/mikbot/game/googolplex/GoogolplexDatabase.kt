package dev.schlaubi.mikbot.game.googolplex

import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object GoogolplexDatabase : KoinComponent {
    val stats = database.getCollection<UserGameStats>("googolplex_stats")
}
