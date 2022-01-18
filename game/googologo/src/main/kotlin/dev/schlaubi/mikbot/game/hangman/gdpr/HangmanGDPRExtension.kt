package dev.schlaubi.mikbot.game.hangman.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.hangman.HangmanDatabase
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class HangmanGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(UnoStatsDataPoint, HangmanProcessDataPoint)

    object UnoStatsDataPoint : GameStatisticsDataPoint("hangman", "gdpr.stats.name", "gdpr.stats.description") {
        override val collection: CoroutineCollection<UserGameStats> = HangmanDatabase.stats
    }

    @Suppress("PrivatePropertyName")
    private val HangmanProcessDataPoint = ProcessedData("hangman", "gdpr.processed_data.description", null)
}
