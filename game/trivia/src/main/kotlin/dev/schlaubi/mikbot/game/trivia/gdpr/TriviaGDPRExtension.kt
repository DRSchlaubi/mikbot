package dev.schlaubi.mikbot.game.trivia.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.trivia.TriviaDatabase
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class TriviaGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(TriviaStatsDataPoint, TriviaProcessDataPoint)
}

object TriviaStatsDataPoint : GameStatisticsDataPoint("trivia", "gdpr.stats.name", "gdpr.stats.description") {
    override val collection: CoroutineCollection<UserGameStats> = TriviaDatabase.stats
}

val TriviaProcessDataPoint = ProcessedData("trivia", "gdpr.processed_data.description", null)
