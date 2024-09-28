package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.QueuedTrack
import dev.schlaubi.mikmusic.player.addAutoPlaySongs
import dev.schlaubi.mikmusic.util.format
import kotlin.time.Duration.Companion.minutes

suspend fun MusicModule.queueCommand() = ephemeralSlashCommand {
    name = "queue"
    description = "commands.queue.description"
    musicControlContexts()

    check {
        anyMusicPlaying(this@queueCommand)
    }

    action {
        if (musicPlayer.queuedTracks.isEmpty()) {
            val track = musicPlayer.playingTrack
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
            timeoutSeconds = 10.minutes.inWholeSeconds

            forList(user, musicPlayer.queuedTracks, QueuedTrack::format, { current, total ->
                translate("music.queue.info.title", arrayOf(current.toString(), total.toString()))
            }) {
                val playingTrack = musicPlayer.playingTrack
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
