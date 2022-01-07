package dev.schlaubi.mikbot.game.music_quiz

import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object MusicQuizDatabase : KoinComponent {
    val stats = database.getCollection<UserGameStats>("song_quiz_stats")
    val likedSongs = database.getCollection<LikedSongs>("liked_songs")
}
