package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistRenameArguments : PlaylistArguments() {
    val newName by string {
        name = "new_name"
        description = "commands.playlist.rename.arguments.new_name.description"
    }
}

fun PlaylistModule.renameCommand() = ephemeralSubCommand(::PlaylistRenameArguments) {
    name = "rename"
    description = "commands.rename.description"

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
