package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import kotlin.reflect.KMutableProperty1

suspend fun MusicModule.schedulerCommands() {
    suspend fun EphemeralSlashCommandContext<*>.schedulerOption(
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

    ephemeralSlashCommand {
        name = "repeat"
        description = "Toggles repeat mode"

        action {
            schedulerOption(
                MusicPlayer::repeat,
                MusicPlayer::shuffle, MusicPlayer::loopQueue,
                enabled = "commands.repeat.enabled",
                disabled = "commands.repeat.disabled"
            )
        }
    }

    ephemeralSlashCommand {
        name = "loop-queueTracks"
        description = "Toggles loop queueTracks mode (Looping the queueTracks over and over)"

        action {
            schedulerOption(
                MusicPlayer::loopQueue,
                MusicPlayer::shuffle, MusicPlayer::repeat,
                enabled = "commands.loop_queue.enabled",
                disabled = "commands.loop_queue.disabled"
            )
        }
    }

    ephemeralSlashCommand {
        name = "shuffle"
        description = "Toggles shuffle mode"

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
