package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.format

class PlaylistRemoveArguments : PlaylistArguments() {
    val index by int("index", "The index of the track to remove")
}

fun PlaylistModule.removeCommand() = playlistSubCommand(::PlaylistRemoveArguments) {
    name = "remove"
    description = "Removes a Track from the playlist"

    action {
        checkPermissions { playlist ->
            val index = arguments.index - 1
            val item = playlist.songs.getOrNull(index)
            if (item == null) {
                respond {
                    content = translate("commands.playlist.remove.too_high_index")
                }

                return@action
            }

            database.playlists.save(playlist.copy(songs = playlist.songs.toMutableList().apply {
                removeAt(index) // this might be a dupe, so we remove by index
            }))

            respond {
                content = translate("commands.playlist.remove.removed", arrayOf(item.format(musicPlayer), playlist.name))
            }
        }
    }
}
