package dev.schlaubi.mikmusic.playlist.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.util.mapToQueuedTrack

class LoadArguments : PlaylistArguments()

fun PlaylistModule.loadCommand() = ephemeralSubCommand(::LoadArguments) {
    name = "load"
    description = "commands.playlist.load.description"

    check {
        joinSameChannelCheck(bot)
    }

    action {
        val playlist = getPlaylist()
        PlaylistDatabase.updatePlaylistUsages(playlist)

        musicPlayer.queueTrack(
            force = false, onTop = false,
            tracks = playlist.getTracks(musicPlayer.node).mapToQueuedTrack(user)
        )

        respond {
            content = translate("command.playlist.load.queued", arrayOf(playlist.songs.size, playlist.name))
        }
    }
}
