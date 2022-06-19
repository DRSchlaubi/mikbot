package dev.schlaubi.mikmusic.playlist

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database

object PlaylistDatabase : KordExKoinComponent {
    val collection = database.getCollection<Playlist>("playlists")

    suspend fun updatePlaylistUsages(playlist: Playlist) {
        collection.save(playlist.copy(usages = playlist.usages + 1))
    }
}
