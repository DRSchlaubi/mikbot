package dev.schlaubi.mikbot.core.i18n.database.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.schlaubi.mikbot.core.i18n.database.LangaugeUser
import dev.schlaubi.mikbot.core.i18n.database.LanguageDatabase
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import java.util.*

private class LanguageArguments : Arguments() {
    val language by stringChoice {
        name = "language"
        description = "The language you want to use"

        choice("German", SupportedLocales.GERMAN.toLanguageTag())
        choice("Englisch", SupportedLocales.ENGLISH.toLanguageTag())
        choice("Italian", Locale.ITALIAN.toLanguageTag())
    }
}

suspend fun SettingsModule.languageCommand() {
    ephemeralSlashCommand(::LanguageArguments) {
        name = "language"
        description = "Changed the language of the bot"

        action {
            val locale = Locale.forLanguageTag(arguments.language)

            val botUser = LanguageDatabase.collection.findOneById(user.id)
            val newUser = botUser?.copy(locale = locale) ?: LangaugeUser(user.id, locale)
            LanguageDatabase.collection.save(newUser)

            respond { content = translate("commands.language.changed", arrayOf(arguments.language)) }
        }
    }
}
