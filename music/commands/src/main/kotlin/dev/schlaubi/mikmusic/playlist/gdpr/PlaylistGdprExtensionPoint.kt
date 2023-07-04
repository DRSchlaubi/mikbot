package dev.schlaubi.mikmusic.playlist.gdpr

import dev.kord.core.entity.User
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.eq
import org.pf4j.Extension

@Extension
class PlaylistGdprExtensionPoint : GDPRExtensionPoint {
    override fun provideDataPoints(): List<DataPoint> = listOf(PlaylistDataPoint)
}

// Data point for stored playlists
val PlaylistDataPoint: PermanentlyStoredDataPoint = PlaylistDataPointImpl

private object PlaylistDataPointImpl :
    PermanentlyStoredDataPoint("music", "gdpr.playlists.name", "gdpr.playlists.description", null) {
    override suspend fun deleteFor(user: User) {
        PlaylistDatabase.collection.deleteMany(Playlist::authorId eq user.id)
    }

    override suspend fun requestFor(user: User): List<String> =
        listOf("All Playlists: `/playlist list`", "Specific Playlist: `/playlist info <name>`")
}
