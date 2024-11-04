package dev.schlaubi.mikmusic.playlist.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.or

class PlayListListArguments : Arguments() {
    val onlyMine by defaultingBoolean {
        name = MusicTranslations.Commands.Playlist.List.Arguments.OnlyMine.name
        description = MusicTranslations.Commands.Playlist.List.Arguments.OnlyMine.description
        defaultValue = false
    }
}

fun PlaylistModule.listCommand() = ephemeralSubCommand(::PlayListListArguments) {
    name = MusicTranslations.Commands.Playlist.List.name
    description = MusicTranslations.Commands.Playlist.List.description

    action {
        val myPlaylists = Playlist::authorId eq user.id
        val filter = if (arguments.onlyMine) {
            myPlaylists
        } else {
            or(myPlaylists, Playlist::public eq true)
        }

        val playlistCount = PlaylistDatabase.collection.countDocuments(filter)

        if (playlistCount == 0L) {
            respond {
                content = translate(MusicTranslations.Commands.Playlist.List.empty)
            }
            return@action
        }
        val playlists = PlaylistDatabase.collection.find(filter).toList()

        val tracks = translate(MusicTranslations.Music.General.tracks)
        editingPaginator {
            forList(
                user, playlists, { "${it.name} - ${it.songs.size} $tracks by <@${it.authorId}>" },
                { current, total ->
                    translate(
                        MusicTranslations.Commands.Playlist.List.Paginator.title,
                        current.toString(), total.toString()
                    )
                }
            )
        }.send()
    }
}
