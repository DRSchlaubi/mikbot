package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.anyMusicPlaying
import dev.schlaubi.musicbot.module.music.player.ChapterQueuedTrack

suspend fun MusicModule.nextCommand() = ephemeralControlSlashCommand {
    name = "next"
    description = "Skips to the next chapter or song if the current song has no chapters"

    check {
        anyMusicPlaying(this@nextCommand)
    }

    action {
        val chapterSong = musicPlayer.playingTrack as? ChapterQueuedTrack
        if (chapterSong == null || chapterSong.isOnLast) {
            if (musicPlayer.queuedTracks.isEmpty()) {
                respond { content = translate("commands.skip.empty") }
                return@action
            }
            respond { content = translate("commands.skip.skipped") }
        } else {
            musicPlayer.skipChapter()

            respond { content = translate("commands.next.skipped_chapter") }
        }
    }
}
