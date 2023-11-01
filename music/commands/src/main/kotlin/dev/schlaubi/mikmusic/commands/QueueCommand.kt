package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.addAutoPlaySongs
import dev.schlaubi.mikmusic.util.format

suspend fun MusicModule.queueCommand() = ephemeralSlashCommand {
    name = "queue"
    description = "commands.queue.description"

    check {
        anyMusicPlaying(this@queueCommand)
    }

    action {
        if (musicPlayer.queuedTracks.isEmpty()) {
            val track = player.playingTrack
            if (track != null) {
                respond {
                    content = translate("commands.queue.now_playing", arrayOf(track.format()))
                }
            } else {
                respond {
                    content = translate("commands.queue.no_songs")
                }
            }
            return@action
        }

        editingPaginator {
            forList(user, musicPlayer.queuedTracks, { (track) -> track.format() }, { current, total ->
                translate("music.queue.info.title", arrayOf(current.toString(), total.toString()))
            }) {
                val playingTrack = player.playingTrack
                if (playingTrack != null) {
                    field {
                        name = translate("music.queue.now_playing")
                        value = playingTrack.format(musicPlayer.repeat)
                    }
                }

                if (musicPlayer.shuffle || musicPlayer.loopQueue) {
                    field {
                        name = translate("music.queueTracks.order")
                        value = if (musicPlayer.shuffle) "\uD83D\uDD00" else "\uD83D\uDD01"
                    }
                }

                musicPlayer.addAutoPlaySongs(::translate)
            }
        }.send()
    }
}
