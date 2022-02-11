package dev.schlaubi.mikbot.game.connect_four.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.gdpr.GameStatisticsDataPoint
import dev.schlaubi.mikbot.game.connect_four.Connect4Database
import org.litote.kmongo.coroutine.CoroutineCollection
import org.pf4j.Extension

@Extension
class Connect4GDPRExtensionPoint : GDPRExtensionPoint {

    override fun provideDataPoints(): List<DataPoint> = listOf(Connect4StatsDataPoint, Connect4ProcessDataPoint)

    object Connect4StatsDataPoint :
        GameStatisticsDataPoint("connect_four", "gdpr.stats.name", "gdpr.stats.description") {
        override val collection: CoroutineCollection<UserGameStats> = Connect4Database.stats
    }

    @Suppress("PrivatePropertyName")
    private val Connect4ProcessDataPoint = ProcessedData("connect_four", "gdpr.processed_data.description", null)
}
