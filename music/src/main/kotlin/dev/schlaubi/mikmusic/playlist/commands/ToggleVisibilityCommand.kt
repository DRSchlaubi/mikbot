package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistToggleVisibilityCommand : PlaylistArguments()

fun PlaylistModule.toggleVisibilityCommand() = ephemeralSubCommand(::PlaylistToggleVisibilityCommand) {
    name = "toggle-visibility"
    description = "Toggles whether other users can find this playlist or not"

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
