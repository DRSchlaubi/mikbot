package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.MusicPlayer
import kotlin.reflect.KMutableProperty1

suspend fun MusicModule.schedulerCommands() {
    suspend fun EphemeralSlashCommandContext<*, *>.schedulerOption(
        myProperty: KMutableProperty1<MusicPlayer, Boolean>,
        vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
        enabled: String,
        disabled: String,
        additional: suspend (Boolean) -> Unit = {}
    ) {
        checkOtherSchedulerOptions(myProperty, *properties) { gotEnabled ->
            val translateKey = if (gotEnabled) enabled else disabled

            additional(gotEnabled)

            respond {
                content = translate(translateKey)
            }
        }
    }

    ephemeralControlSlashCommand {
        name = "repeat"
        description = "Toggles repeat mode"
        musicControlContexts()

        action {
            schedulerOption(
                MusicPlayer::repeat,
                MusicPlayer::shuffle, MusicPlayer::loopQueue,
                enabled = "commands.repeat.enabled",
                disabled = "commands.repeat.disabled"
            )
        }
    }

    ephemeralControlSlashCommand {
        name = "loop-queue"
        description = "Toggles loop queueTracks mode (Looping the queueTracks over and over)"
        musicControlContexts()

        action {
            schedulerOption(
                MusicPlayer::loopQueue,
                MusicPlayer::shuffle, MusicPlayer::repeat,
                enabled = "commands.loop_queue.enabled",
                disabled = "commands.loop_queue.disabled"
            )
        }
    }

    ephemeralControlSlashCommand {
        name = "shuffle"
        description = "Toggles shuffle mode"
        musicControlContexts()

        action {
            schedulerOption(
                MusicPlayer::shuffle,
                MusicPlayer::repeat, MusicPlayer::loopQueue,
                enabled = "commands.shuffle.enabled",
                disabled = "commands.shuffle.disabled"
            )
        }
    }
}
