package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.utils.database

class PlaylistDeleteArguments : PlaylistArguments()

fun PlaylistModule.deleteCommand() = playlistSubCommand(::PlaylistDeleteArguments) {
    name = "delete"
    description = "Deletes a playlist"

    action {
        checkPermissions { playlist ->
            database.playlists.deleteOneById(playlist.id)

            respond {
                content = translate("commands.playlist.delete.deleted", arrayOf(playlist.name))
            }
        }
    }
}
