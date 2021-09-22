package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.editingPaginator
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.format

suspend fun MusicModule.queueCommand() = ephemeralSlashCommand {
    name = "queue"
    description = "Shows the current queue"

    action {
        editingPaginator {
            forList(user, musicPlayer.queuedTracks, { it.format() }, { current, total ->
                translate("music.queue.info.title", arrayOf(current.toString(), total.toString()))
            }) {
                val playingTrack = player.playingTrack
                if (playingTrack != null) {
                    field {
                        name = translate("music.queue.now_playing")
                        value = playingTrack.format()
                    }
                }
            }
        }.send()
    }
}
