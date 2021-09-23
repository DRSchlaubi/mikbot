package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.utils.database

class PlaylistToggleVisibilityCommand : PlaylistArguments()

fun PlaylistModule.toggleVisibilityCommand() = playlistSubCommand(::PlaylistToggleVisibilityCommand) {
    name = "toggle-visibility"
    description = "Toggles whether other users can find this playlist or not"

    action {
        checkPermissions { playlist ->
            database.playlists.save(playlist.copy(public = !playlist.public))

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
