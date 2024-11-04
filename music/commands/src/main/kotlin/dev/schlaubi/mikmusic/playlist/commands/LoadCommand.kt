package dev.schlaubi.mikmusic.playlist.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.queue.SchedulingArguments
import dev.schlaubi.mikmusic.playlist.PlaylistDatabase
import dev.schlaubi.mikmusic.util.mapToQueuedTrack

class LoadArguments : SchedulingArguments(), PlaylistOptions {
    override val name by playlistName(onlyMine = false)
}

fun PlaylistModule.loadCommand() = ephemeralSubCommand(::LoadArguments) {
    name = MusicTranslations.Commands.Playlist.Load.name
    description = MusicTranslations.Commands.Playlist.Load.description

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
            content = translate(MusicTranslations.Command.Playlist.Load.queued, playlist.songs.size, playlist.name)
        }
    }
}
