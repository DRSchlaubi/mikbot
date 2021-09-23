package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.utils.database

class PlaylistRenameArguments : PlaylistArguments() {
    val newName by string("new_name", "The new name of the playlist")
}

fun PlaylistModule.renameCommand() = playlistSubCommand(::PlaylistRenameArguments) {
    name = "rename"
    description = "Renames a playlist"

    action {
        checkPermissions { playlist ->
            checkName(arguments.newName, playlist.public) {
                database.playlists.save(playlist.copy(name = arguments.newName))

                respond {
                    content = translate("commands.playlist.rename.renamed", arrayOf(playlist.name, arguments.newName))
                }
            }
        }
    }
}
