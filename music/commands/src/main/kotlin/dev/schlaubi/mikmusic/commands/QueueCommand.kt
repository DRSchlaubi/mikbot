package dev.schlaubi.mikmusic.commands

import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kord.rest.builder.message.embed
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.checks.anyMusicPlaying
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.QueuedTrack
import dev.schlaubi.mikmusic.player.addAutoPlaySongs
import dev.schlaubi.mikmusic.util.format
import kotlin.time.Duration.Companion.minutes

suspend fun MusicModule.queueCommand() = ephemeralSlashCommand {
    name = MusicTranslations.Commands.Queue.name
    description = MusicTranslations.Commands.Queue.description
    musicControlContexts()

    check {
        anyMusicPlaying(this@queueCommand)
    }

    action {
        if (musicPlayer.queue.isEmpty()) {
            val track = musicPlayer.playingTrack
            if (track != null) {
                respond {
                    embed {
                        musicPlayer.addAutoPlaySongs(this@action)
                        description = translate(MusicTranslations.Commands.Queue.nowPlaying, track.format())
                    }
                }
            } else {
                respond {
                    content = translate(MusicTranslations.Commands.Queue.noSongs)
                }
            }
            return@action
        }

        editingPaginator {
            timeoutSeconds = 10.minutes.inWholeSeconds

            forList(user, musicPlayer.queuedTracks, QueuedTrack::format, { current, total ->
                translate(MusicTranslations.Music.Queue.Info.title, current.toString(), total.toString())
            }) {
                val playingTrack = musicPlayer.playingTrack
                if (playingTrack != null) {
                    field {
                        name = translate(MusicTranslations.Music.Queue.nowPlaying)
                        value = playingTrack.format(musicPlayer.repeat)
                    }
                }

                if (musicPlayer.shuffle || musicPlayer.loopQueue) {
                    field {
                        name = translate(MusicTranslations.Music.QueueTracks.order)
                        value = if (musicPlayer.shuffle) "\uD83D\uDD00" else "\uD83D\uDD01"
                    }
                }

                musicPlayer.addAutoPlaySongs(this@action)
            }
        }.send()
    }
}
