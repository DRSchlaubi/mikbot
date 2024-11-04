package dev.schlaubi.mikmusic.playlist.commands

import dev.kordex.core.commands.converters.impl.int
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase

class PlaylistRemoveArguments : PlaylistArguments() {
    val index by int {
        name = MusicTranslations.Commands.Playlist.Remove.Arguments.Index.name
        description = MusicTranslations.Commands.Playlist.Remove.Arguments.Index.description
    }
}

fun PlaylistModule.removeCommand() = ephemeralSubCommand(::PlaylistRemoveArguments) {
    name = MusicTranslations.Commands.Playlist.Remove.name
    description = MusicTranslations.Commands.Playlist.Remove.description

    action {
        checkPermissions { playlist ->
            val index = arguments.index - 1
            val item = playlist.songs.getOrNull(index)
            if (item == null) {
                respond {
                    content = translate(MusicTranslations.Commands.Playlist.Remove.tooHighIndex)
                }

                return@action
            }

            PlaylistDatabase.collection.save(
                playlist.copy(
                    songs = playlist.songs.toMutableList().apply {
                        removeAt(index) // this might be a dupe, so we remove by index
                    }
                )
            )

            respond {
                content = translate(MusicTranslations.Commands.Playlist.Remove.removed, item.toTrack(musicPlayer.node), playlist.name)
            }
        }
    }
}
