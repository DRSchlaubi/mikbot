package dev.schlaubi.mikmusic.playlist.commands

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.util.format

class PlaylistSongsArguments : PlaylistArguments()

fun PlaylistModule.songsCommand() = ephemeralSubCommand(::PlaylistSongsArguments) {
    name = MusicTranslations.Commands.Playlist.Songs.name
    description = MusicTranslations.Commands.Playlist.Songs.description

    action {
        val playlist = getPlaylist()
        if (playlist.songs.isEmpty()) {
            respond {
                content = translate(MusicTranslations.Commands.Playlist.Songs.isEmpty)
            }
            return@action
        }

        val tracks = playlist.getTracks(musicPlayer.node)

        editingPaginator {
            forList(
                user, tracks, Track::format,
                { current, total ->
                    translate(
                        MusicTranslations.Commands.Playlist.Songs.Paginator.title,
                        arrayOf(playlist.name, current.toString(), total.toString())
                    )
                }
            )
        }.send()
    }
}
