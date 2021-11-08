package dev.schlaubi.mikbot.game.music_quiz.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.music_quiz.MusicQuizDatabase
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class MusicQuizGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(UnoStatsDataPoint, UnoProcessDataPoint)
}

object UnoStatsDataPoint : GameStatisticsDataPoint("song_quiz", "gdpr.stats.name", "gdpr.stats.description") {
    override val collection: CoroutineCollection<UserGameStats> = MusicQuizDatabase.stats
}

val UnoProcessDataPoint = ProcessedData("song_quiz", "gdpr.processed_data.description", null)
