package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.interactions.editingPaginator
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.format

class PlaylistSongsArguments : PlaylistArguments()

fun PlaylistModule.songsCommand() = playlistSubCommand(::PlaylistSongsArguments) {
    name = "songs"
    description = "Shows all the songs which are in a playlist"

    action {
        val playlist = getPlaylist()
        if (playlist.songs.isEmpty()) {
            respond {
                content = translate("commands.playlist.songs.is_empty")
            }
            return@action
        }

        editingPaginator {
            forList(user, playlist.songs, { it.format(musicPlayer) },
                { current, total ->
                    translate(
                        "commands.playlist.songs.paginator.title",
                        arrayOf(playlist.name, current.toString(), total.toString())
                    )
                }
            )
        }.send()
    }
}

