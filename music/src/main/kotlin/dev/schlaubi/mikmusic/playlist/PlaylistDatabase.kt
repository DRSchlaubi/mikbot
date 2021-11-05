package dev.schlaubi.mikmusic.playlist

import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent

object PlaylistDatabase : KoinComponent {
    val collection = database.getCollection<Playlist>("playlists")

    suspend fun updatePlaylistUsages(playlist: Playlist) {
        collection.save(playlist.copy(usages = playlist.usages + 1))
    }
}
