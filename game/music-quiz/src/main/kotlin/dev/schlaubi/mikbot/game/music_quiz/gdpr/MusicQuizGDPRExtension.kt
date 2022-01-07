package dev.schlaubi.mikbot.game.music_quiz.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.music_quiz.MusicQuizDatabase
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class MusicQuizGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(MusicQuizStatsDataPoint, MusicQuizProcessDataPoint, LikedSongsDataPoint)
}

object MusicQuizStatsDataPoint : GameStatisticsDataPoint("song_quiz", "gdpr.stats.name", "gdpr.stats.description") {
    override val collection: CoroutineCollection<UserGameStats> = MusicQuizDatabase.stats
}

object LikedSongsDataPoint :
    PermanentlyStoredDataPoint("song-quiz", "gdpr.liked_songs.name", "gdpr.liked_songs.description") {
    override suspend fun requestFor(user: User): List<String> = listOf("/song-likes list")

    override suspend fun deleteFor(user: User) {
        MusicQuizDatabase.likedSongs.deleteOneById(user.id)
    }
}

val MusicQuizProcessDataPoint = ProcessedData("song_quiz", "gdpr.processed_data.description", null)
