package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.utils.safeGuild
import dev.schlaubi.musicbot.utils.unSetableBoolean

class SchedulerSettingsArguments : Arguments() {
    val repeat by unSetableBoolean("repeat", "Whether to repeat the queue by default or not")
    val loopQueue by unSetableBoolean("loop_queue", "Whether to repeat the entire queue by default or not")
    val shuffle by unSetableBoolean("shuffle", "Whether to shuffle the queue by default or not")
    val volume by optionalInt("volume", "The default volume for channels")
}

suspend fun SettingsModule.optionsCommand() {
    suspend fun <T> EphemeralSlashCommandContext<SchedulerSettingsArguments>.doUpdate(
        findObject: suspend (EphemeralSlashCommandContext<SchedulerSettingsArguments>) -> T,
        mapObject: (T) -> SchedulerSettings?,
        save: suspend T.(SchedulerSettings?) -> Unit,
    ) {
        val obj = findObject(this)
        val settings = mapObject(obj) ?: SchedulerSettings()

        val newSettings = settings.copy(
            // fallback to predefined option, to not reset each time, therefore ?:
            repeat = arguments.repeat?.value ?: settings.repeat,
            loopQueue = arguments.loopQueue?.value ?: settings.loopQueue,
            shuffle = arguments.shuffle?.value ?: settings.shuffle,
            volume = arguments.volume?.toFloat()?.div(100) ?: settings.volume
        )

        save(obj, newSettings)

        respond {
            content = translate("command.scheduler_settings.updated")
        }
    }

    ephemeralSlashCommand(::SchedulerSettingsArguments) {
        name = "guild-options"
        description = "Updates the settings of Scheduling for this guild"

        guildAdminOnly()

        action {
            doUpdate({
                database.guildSettings.findGuild(safeGuild)
            }, BotGuild::defaultSchedulerSettings) {
            database.guildSettings.save(copy(defaultSchedulerSettings = it))
        }
        }
    }

//    ephemeralSlashCommand(::SchedulerSettingsArguments) {
//        name = "update-user-scheduler"
//        description = "Updates the settings of Scheduling for yourself guild"
//
//        action {
//            doUpdate({
//                database.users.findUser(user)
//            }, BotUser::defaultSchedulerSettings) {
//                database.users.save(copy(defaultSchedulerSettings = it))
//            }
//        }
//    }
}
