package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistToggleVisibilityCommand : PlaylistArguments()

fun PlaylistModule.toggleVisibilityCommand() = ephemeralSubCommand(::PlaylistToggleVisibilityCommand) {
    name = MusicTranslations.Commands.Playlist.ToggleVisibility.name
    description = MusicTranslations.Commands.Playlist.ToggleVisibility.description

    action {
        checkPermissions { playlist ->
            PlaylistDatabase.collection.save(playlist.copy(public = !playlist.public))

            respond {
                content = if (!playlist.public) {
                    translate(MusicTranslations.Commands.Playlist.ToggleVisibility.on)
                } else {
                    translate(MusicTranslations.Commands.Playlist.ToggleVisibility.off)
                }
            }
        }
    }
}
