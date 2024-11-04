package dev.schlaubi.mikmusic.gdpr

import dev.schlaubi.mikbot.core.gdpr.api.AnonymizedData
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.ProcessedData
import dev.schlaubi.mikbot.translations.MusicTranslations
import org.pf4j.Extension

@Extension
class MusicGdprExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(AutoCompleteDataPoint, MusicChannelDataPoint)
}

// Data sent to Google for AutoComplete on search commands
val AutoCompleteDataPoint =
    AnonymizedData(
        MusicTranslations.Gdpr.AutoComplete.description,
        MusicTranslations.Gdpr.AutoComplete.Sharing.description
    )

// Data required for music-channel
val MusicChannelDataPoint = ProcessedData(MusicTranslations.Gdpr.MusicChannel.description, null)
