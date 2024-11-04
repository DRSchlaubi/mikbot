package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistToggleVisibilityCommand : PlaylistArguments()

fun PlaylistModule.toggleVisibilityCommand() = ephemeralSubCommand(::PlaylistToggleVisibilityCommand) {
    name = MusicTranslations.Commands.Playlist.Toggle_visibility.name
    description = MusicTranslations.Commands.Playlist.Toggle_visibility.description

    action {
        checkPermissions { playlist ->
            PlaylistDatabase.collection.save(playlist.copy(public = !playlist.public))

            respond {
                content = if (!playlist.public) {
                    translate(MusicTranslations.Commands.Playlist.Toggle_visibility.on)
                } else {
                    translate(MusicTranslations.Commands.Playlist.Toggle_visibility.off)
                }
            }
        }
    }
}
