package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistDeleteArguments : PlaylistArguments()

fun PlaylistModule.deleteCommand() = ephemeralSubCommand(::PlaylistDeleteArguments) {
    name = "delete"
    description = "commands.delete.description"

    action {
        checkPermissions { playlist ->
            PlaylistDatabase.collection.deleteOneById(playlist.id)

            respond {
                content = translate("commands.playlist.delete.deleted", arrayOf(playlist.name))
            }
        }
    }
}
