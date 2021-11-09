package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.forFlow
import dev.schlaubi.mikmusic.playlist.Playlist
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.or

class PlayListListArguments : Arguments() {
    val onlyMine by defaultingBoolean("only_mine", "Just show my playlist", false)
}

fun PlaylistModule.listCommand() = ephemeralSubCommand(::PlayListListArguments) {
    name = "list"
    description = "Shows all playlists, which are available to you"

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
        val playlists = PlaylistDatabase.collection.find(filter).toFlow()

        val tracks = translate("music.general.tracks")
        editingPaginator {
            forFlow(
                user, playlistCount, playlists, { "${it.name} - ${it.songs.size} $tracks by <@${it.authorId.asString}>" },
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
