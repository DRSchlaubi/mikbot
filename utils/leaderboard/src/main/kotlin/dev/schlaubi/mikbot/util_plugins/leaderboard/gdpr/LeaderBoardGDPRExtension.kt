package dev.schlaubi.mikbot.util_plugins.leaderboard.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import org.pf4j.Extension

@Extension
class LeaderBoardGDPRExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(LeaderBoardDataPoint)
}
