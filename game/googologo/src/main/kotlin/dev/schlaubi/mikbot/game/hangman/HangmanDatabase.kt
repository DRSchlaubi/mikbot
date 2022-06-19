package dev.schlaubi.mikbot.game.hangman

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object HangmanDatabase : KordExKoinComponent {
    val stats = database.getCollection<UserGameStats>("hangman_stats")
}
