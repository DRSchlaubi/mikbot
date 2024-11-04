package dev.schlaubi.mikmusic.playlist.commands

import dev.kordex.core.commands.converters.impl.string
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistRenameArguments : PlaylistArguments() {
    val newName by string {
        name = MusicTranslations.Commands.Playlist.Rename.Arguments.NewName.name
        description = MusicTranslations.Commands.Playlist.Rename.Arguments.NewName.description
    }
}

fun PlaylistModule.renameCommand() = ephemeralSubCommand(::PlaylistRenameArguments) {
    name = MusicTranslations.Commands.Playlist.Rename.name
    description = MusicTranslations.Commands.Playlist.Rename.description

    action {
        checkPermissions { playlist ->
            checkName(arguments.newName, playlist.public) {
                PlaylistDatabase.collection.save(playlist.copy(name = arguments.newName))

                respond {
                    content = translate(MusicTranslations.Commands.Playlist.Rename.renamed, playlist.name, arguments.newName)
                }
            }
        }
    }
}
