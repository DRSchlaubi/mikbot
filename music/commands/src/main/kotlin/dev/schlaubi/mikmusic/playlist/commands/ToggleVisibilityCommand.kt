package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistToggleVisibilityCommand : PlaylistArguments()

fun PlaylistModule.toggleVisibilityCommand() = ephemeralSubCommand(::PlaylistToggleVisibilityCommand) {
    name = "toggle-visibility"
    description = "commands.playlist.toggle_visibility.description"

    action {
        checkPermissions { playlist ->
            PlaylistDatabase.collection.save(playlist.copy(public = !playlist.public))

            respond {
                content = if (!playlist.public) {
                    translate("commands.playlist.toggle_visibility.on")
                } else {
                    translate("commands.playlist.toggle_visibility.off")
                }
            }
        }
    }
}
