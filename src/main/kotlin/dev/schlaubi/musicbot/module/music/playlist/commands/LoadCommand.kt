package dev.schlaubi.musicbot.module.music.playlist.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.checks.joinSameChannelCheck
import dev.schlaubi.musicbot.utils.database
import dev.schlaubi.musicbot.utils.mapToQueuedTrack

class LoadArguments : PlaylistArguments()

fun PlaylistModule.loadCommand() = ephemeralSubCommand(::LoadArguments) {
    name = "load"
    description = "Queues a playlist"

    check {
        joinSameChannelCheck(bot)
    }

    action {
        val playlist = getPlaylist()
        database.updatePlaylistUsages(playlist)

        musicPlayer.queueTrack(
            force = false, onTop = false,
            tracks = playlist.songs.mapToQueuedTrack(user)
        )

        respond {
            content = translate("command.playlist.load.queued", arrayOf(playlist.songs.size, playlist.name))
        }
    }
}
