package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.editingPaginator
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.anyMusicPlaying
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.format

suspend fun MusicModule.queueCommand() = ephemeralSlashCommand {
    name = "queue"
    description = "Shows the current queue"

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
        }

        editingPaginator {
            forList(user, musicPlayer.queuedTracks, { it.format() }, { current, total ->
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
            }
        }.send()
    }
}
