package dev.schlaubi.mikbot.util_plugins.birthdays.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.util_plugins.birthdays.database.BirthdayDatabase
import dev.schlaubi.mikbot.util_plugins.birthdays.database.UserBirthday
import dev.schlaubi.mikbot.util_plugins.birthdays.server.receiveTimeZone
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.datetime.toKotlinInstant
import java.text.DateFormat
import java.text.ParseException
import java.util.*

class SetArguments : Arguments() {
    val birthday by optionalString {
        name = "birthday"
        description = "commands.birthday.set.arguments.birthday.description"
    }
}

suspend fun SlashCommand<*, *>.setCommand() = ephemeralSubCommand(::SetArguments) {
    name = "set"
    description = "commands.birthday.set.description"

    action {
        if (arguments.birthday != null) {
            val locale = event.interaction.locale?.asJavaLocale()
                ?: event.interaction.guildLocale?.asJavaLocale()
                ?: Locale.getDefault()
            val format = DateFormat.getDateInstance(DateFormat.SHORT, locale)
            val key = generateNonce()
            val url = buildBotUrl {
                appendPathSegments("birthdays", "timezone", key)
            }
            val message = respond {
                embed {
                    title = translate("commands.birthday.set.timezone.title")
                    description = translate("commands.birthday.set.timezone.description", arrayOf(url))
                }
            }
            val date = try {
                format.parse(arguments.birthday).toInstant().toKotlinInstant()
            } catch (e: ParseException) {
                discordError(translate("commands.birthday.set.invalid_date"))
            }
            val timeZone = receiveTimeZone(key)
            if (timeZone == null) {
                message.edit {
                    content = translate("commands.birthdays.set.timeout")
                    embeds = mutableListOf()
                }
                return@action
            }
            val userBirthday = UserBirthday(user.id, date, timeZone)
            BirthdayDatabase.birthdays.save(userBirthday)

            message.edit {
                embeds = mutableListOf()
                content = translate(
                    "commands.birthday.set.success",
                    arrayOf(date.toMessageFormat(DiscordTimestampStyle.LongDate))
                )
            }
        } else {
            BirthdayDatabase.birthdays.deleteOneById(user.id)

            respond {
                content = translate("commands.birthday.delete")
            }
        }
    }
}
