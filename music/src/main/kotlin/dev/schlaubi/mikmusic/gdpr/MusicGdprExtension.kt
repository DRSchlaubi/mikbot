package dev.schlaubi.mikmusic.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.*
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.eq
import org.pf4j.Extension

@Extension
class MusicGdprExtension : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> =
        listOf(PlaylistDataPoint, AutoCompleteDataPoint, MusicChannelDataPoint)
}

// Data point for stored playlists
val PlaylistDataPoint: PermanentlyStoredDataPoint = PlaylistDataPointImpl

// Data sent to Google for AutoComplete on search commands
val AutoCompleteDataPoint =
    AnonymizedData("music", "gdpr.auto_complete.description", "gdpr.auto_complete.sharing.description")

// Data required for music-channel
val MusicChannelDataPoint = ProcessedData("music", "gdpr.music_channel.description", null)

private object PlaylistDataPointImpl :
    PermanentlyStoredDataPoint("music", "gdpr.playlists.name", "gdpr.playlists.description", null) {
    override suspend fun deleteFor(user: User) {
        PlaylistDatabase.collection.deleteMany(Playlist::authorId eq user.id)
    }

    override suspend fun requestFor(user: User): List<String> =
        listOf("All Playlists: `/playlist list`", "Specific Playlist: `/playlist info <name>`")
}
