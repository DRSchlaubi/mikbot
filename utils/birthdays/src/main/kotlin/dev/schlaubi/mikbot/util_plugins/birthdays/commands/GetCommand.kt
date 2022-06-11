package dev.schlaubi.mikbot.util_plugins.birthdays.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalMember
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.toMessageFormat
import dev.schlaubi.mikbot.plugin.api.util.kord
import dev.schlaubi.mikbot.util_plugins.birthdays.calculate
import dev.schlaubi.mikbot.util_plugins.birthdays.database.BirthdayDatabase

class GetArguments : Arguments() {
    val member by optionalMember {
        name = "member"
        description = "commands.birthday.get.arguments.member.description"
    }
}

@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun SlashCommand<*, *>.getCommand() = publicSubCommand(::GetArguments) {
    name = "get"
    description = "commands.birthday.get.description"

    action {
        val birthday = BirthdayDatabase.birthdays.findOneById(arguments.member?.id ?: user.id)

        if (birthday != null) {
            val (_, nextBirthday, days, age) = birthday.calculate()
            val mention = kord.unsafe.user(birthday.id).mention
            respond {
                content = translate(
                    "command.birthday.get.info",
                    arrayOf(mention, age, days, nextBirthday.toMessageFormat(DiscordTimestampStyle.LongDate))
                )
            }
        } else {
            respond {
                content = translate("commands.birthday.get.unknown")
            }
        }
    }
}
