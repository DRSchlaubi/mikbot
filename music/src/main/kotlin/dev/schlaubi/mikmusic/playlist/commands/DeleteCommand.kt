package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistDeleteArguments : PlaylistArguments()

fun PlaylistModule.deleteCommand() = ephemeralSubCommand(::PlaylistDeleteArguments) {
    name = "delete"
    description = "Deletes a playlist"

    action {
        checkPermissions { playlist ->
            PlaylistDatabase.collection.deleteOneById(playlist.id)

            respond {
                content = translate("commands.playlist.delete.deleted", arrayOf(playlist.name))
            }
        }
    }
}
