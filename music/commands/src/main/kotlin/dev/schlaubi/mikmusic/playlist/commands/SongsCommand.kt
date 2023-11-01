package dev.schlaubi.mikmusic.playlist.commands

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.util.format

class PlaylistSongsArguments : PlaylistArguments()

fun PlaylistModule.songsCommand() = ephemeralSubCommand(::PlaylistSongsArguments) {
    name = "songs"
    description = "commands.playlist.songs.description"

    action {
        val playlist = getPlaylist()
        if (playlist.songs.isEmpty()) {
            respond {
                content = translate("commands.playlist.songs.is_empty")
            }
            return@action
        }

        val tracks = playlist.getTracks(musicPlayer.node)

        editingPaginator {
            forList(
                user, tracks, Track::format,
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
