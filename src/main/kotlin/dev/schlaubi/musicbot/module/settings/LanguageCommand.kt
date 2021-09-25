package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.core.io.findUser
import java.util.Locale

private class LanguageArguments : Arguments() {
    val language by stringChoice(
        "language", "The language you want to use",
        mapOf(
            "German" to SupportedLocales.GERMAN.toLanguageTag(),
            "Englisch" to SupportedLocales.ENGLISH.toLanguageTag()
        )
    )
}

suspend fun SettingsModule.languageCommand() {
    ephemeralSlashCommand(::LanguageArguments) {
        name = "language"
        description = "Changed the language of the bot"

        action {
            val locale = Locale.forLanguageTag(arguments.language)

            val botUser = database.users.findUser(user)
            val newUser = botUser.copy(language = locale)
            database.users.save(newUser)

            respond { content = translate("commands.language.changed", arrayOf(arguments.language)) }
        }
    }
}
