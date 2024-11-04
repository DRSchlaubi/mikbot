package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.ChapterQueuedTrack

suspend fun MusicModule.nextCommand() = ephemeralControlSlashCommand {
    name = MusicTranslations.Commands.Next.name
    description = MusicTranslations.Commands.Next.description
    musicControlContexts()

    check {
        anyMusicPlaying(this@nextCommand)
    }

    action {
        val chapterSong = musicPlayer.playingTrack as? ChapterQueuedTrack
        if (chapterSong == null || chapterSong.isOnLast) {
            if (musicPlayer.canSkip) {
                respond { content = translate(MusicTranslations.Commands.Skip.skipped) }
                return@action
            }
            musicPlayer.skip()
            respond { content = translate(MusicTranslations.Commands.Skip.skipped) }
        } else {
            musicPlayer.skipChapter()
            respond { content = translate(MusicTranslations.Commands.Next.skippedChapter) }
        }
    }
}
