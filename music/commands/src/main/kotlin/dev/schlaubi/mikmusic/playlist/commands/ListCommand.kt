package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.or

class PlayListListArguments : Arguments() {
    val onlyMine by defaultingBoolean {
        name = "only_mine"
        description = "commands.playlist.list.arguments.only_mine.description"
        defaultValue = false
    }
}

fun PlaylistModule.listCommand() = ephemeralSubCommand(::PlayListListArguments) {
    name = "list"
    description = "commands.playlist.list.description"

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
                content = translate("commands.playlist.list.empty")
            }
            return@action
        }
        val playlists = PlaylistDatabase.collection.find(filter).toList()

        val tracks = translate("music.general.tracks")
        editingPaginator {
            forList(
                user, playlists, { "${it.name} - ${it.songs.size} $tracks by <@${it.authorId}>" },
                { current, total ->
                    translate(
                        "commands.playlist.list.paginator.title",
                        arrayOf(current.toString(), total.toString())
                    )
                }
            )
        }.send()
    }
}
