package dev.schlaubi.mikbot.game.hangman

import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object HangmanDatabase : KoinComponent {
    val stats = database.getCollection<UserGameStats>("hangman_stats")
}
