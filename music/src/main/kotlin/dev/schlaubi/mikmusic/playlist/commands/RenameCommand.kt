package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistRenameArguments : PlaylistArguments() {
    val newName by string("new_name", "The new name of the playlist")
}

fun PlaylistModule.renameCommand() = ephemeralSubCommand(::PlaylistRenameArguments) {
    name = "rename"
    description = "Renames a playlist"

    action {
        checkPermissions { playlist ->
            checkName(arguments.newName, playlist.public) {
                PlaylistDatabase.collection.save(playlist.copy(name = arguments.newName))

                respond {
                    content = translate("commands.playlist.rename.renamed", arrayOf(playlist.name, arguments.newName))
                }
            }
        }
    }
}
