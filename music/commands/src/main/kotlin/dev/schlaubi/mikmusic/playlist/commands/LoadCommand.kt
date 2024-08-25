package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.queue.SchedulingArguments
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.util.mapToQueuedTrack

class LoadArguments : SchedulingArguments(), PlaylistOptions {
    override val name by playlistName(onlyMine = false)
}

fun PlaylistModule.loadCommand() = ephemeralSubCommand(::LoadArguments) {
    name = "load"
    description = "commands.playlist.load.description"

    musicControlContexts()

    check {
        joinSameChannelCheck(bot)
    }

    action {
        val playlist = getPlaylist()
        PlaylistDatabase.updatePlaylistUsages(playlist)

        musicPlayer.queueTrack(
            force = false, onTop = false,
            tracks = playlist.getTracks(musicPlayer.node).mapToQueuedTrack(user),
            schedulingOptions = arguments
        )

        respond {
            content = translate("command.playlist.load.queued", arrayOf(playlist.songs.size, playlist.name))
        }
    }
}
