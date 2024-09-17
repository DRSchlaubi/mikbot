package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.ChapterQueuedTrack

suspend fun MusicModule.nextCommand() = ephemeralControlSlashCommand {
    name = "next"
    description = "commands.next.description"
    musicControlContexts()

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
