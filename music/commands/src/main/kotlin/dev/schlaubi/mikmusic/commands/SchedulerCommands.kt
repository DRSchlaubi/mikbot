package dev.schlaubi.mikmusic.commands

import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.i18n.types.Key
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.player.MusicPlayer
import kotlin.reflect.KMutableProperty1

suspend fun MusicModule.schedulerCommands() {
    suspend fun EphemeralSlashCommandContext<*, *>.schedulerOption(
        myProperty: KMutableProperty1<MusicPlayer, Boolean>,
        vararg properties: KMutableProperty1<MusicPlayer, Boolean>,
        enabled: Key,
        disabled: Key,
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
        name = MusicTranslations.Commands.Repeat.name
        description = MusicTranslations.Commands.Repeat.description
        musicControlContexts()

        action {
            schedulerOption(
                MusicPlayer::repeat,
                MusicPlayer::shuffle, MusicPlayer::loopQueue,
                enabled = MusicTranslations.Commands.Repeat.enabled,
                disabled = MusicTranslations.Commands.Repeat.disabled,
            )
        }
    }

    ephemeralControlSlashCommand {
        name = MusicTranslations.Commands.Loop_queue.name
        description = MusicTranslations.Commands.Loop_queue.description
        musicControlContexts()

        action {
            schedulerOption(
                MusicPlayer::loopQueue,
                MusicPlayer::shuffle, MusicPlayer::repeat,
                enabled = MusicTranslations.Commands.Loop_queue.enabled,
                disabled = MusicTranslations.Commands.Loop_queue.disabled,
            )
        }
    }

    ephemeralControlSlashCommand {
        name = MusicTranslations.Commands.Shuffle.name
        description = MusicTranslations.Commands.Shuffle.description
        musicControlContexts()

        action {
            schedulerOption(
                MusicPlayer::shuffle,
                MusicPlayer::repeat, MusicPlayer::loopQueue,
                enabled = MusicTranslations.Commands.Shuffle.enabled,
                disabled = MusicTranslations.Commands.Shuffle.disabled,
            )
        }
    }
}
