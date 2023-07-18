package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistRemoveArguments : PlaylistArguments() {
    val index by int {
        name = "index"
        description = "commands.playlist.remove.arguments.index.description"
    }
}

fun PlaylistModule.removeCommand() = ephemeralSubCommand(::PlaylistRemoveArguments) {
    name = "remove"
    description = "commands.playlist.remove.description"

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

            PlaylistDatabase.collection.save(
                playlist.copy(
                    songs = playlist.songs.toMutableList().apply {
                        removeAt(index) // this might be a dupe, so we remove by index
                    }
                )
            )

            respond {
                content = translate("commands.playlist.remove.removed", arrayOf(item.toTrack(musicPlayer.node), playlist.name))
            }
        }
    }
}
