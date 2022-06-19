package dev.schlaubi.mikbot.game.trivia

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object TriviaDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("triva_stats")
}
