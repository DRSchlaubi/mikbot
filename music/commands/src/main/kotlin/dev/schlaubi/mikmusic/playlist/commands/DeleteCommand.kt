package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistDeleteArguments : PlaylistArguments()

fun PlaylistModule.deleteCommand() = ephemeralSubCommand(::PlaylistDeleteArguments) {
    name = MusicTranslations.Commands.Playlist.Delete.name
    description = MusicTranslations.Commands.Playlist.Delete.description

    action {
        checkPermissions { (id, _, playlistName) ->
            PlaylistDatabase.collection.deleteOneById(id)

            respond {
                content = translate(MusicTranslations.Commands.Playlist.Delete.deleted, playlistName)
            }
        }
    }
}
