package dev.schlaubi.mikmusic.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import org.pf4j.Extension

@Extension
class MusicGdprExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(AutoCompleteDataPoint, MusicChannelDataPoint)
}

// Data sent to Google for AutoComplete on search commands
val AutoCompleteDataPoint =
    AnonymizedData("music", "gdpr.auto_complete.description", "gdpr.auto_complete.sharing.description")

// Data required for music-channel
val MusicChannelDataPoint = ProcessedData("music", "gdpr.music_channel.description", null)
