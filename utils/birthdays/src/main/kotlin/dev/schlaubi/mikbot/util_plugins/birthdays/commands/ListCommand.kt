package dev.schlaubi.mikbot.util_plugins.birthdays.commands

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.toMessageFormat
import dev.schlaubi.mikbot.plugin.api.util.forFlow
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.util_plugins.birthdays.calculate
import dev.schlaubi.mikbot.util_plugins.birthdays.database.BirthdayDatabase

@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun SlashCommand<*, *>.listCommand() = publicSubCommand {
    name = "list"
    description = "commands.birthday.list.description"

    action {
        val amount = BirthdayDatabase.birthdays.countDocuments()
        val birthdays = BirthdayDatabase.birthdays.find().toFlow()

        editingPaginator {
            forFlow(
                user,
                amount,
                birthdays,
                {
                    val (birthday, nextBirthday, _, nextAge) = it.calculate()

                    "${kord.unsafe.user(it.id).mention} - ${
                        birthday.toMessageFormat(DiscordTimestampStyle.LongDate)
                    } (${
                        nextBirthday.toMessageFormat(DiscordTimestampStyle.RelativeTime)
                    }) ($nextAge)"
                },
                { current, total -> translate("commands.birthday.list.title", arrayOf(current, total)) },
                enumerate = false
            )
        }.send()
    }
}

