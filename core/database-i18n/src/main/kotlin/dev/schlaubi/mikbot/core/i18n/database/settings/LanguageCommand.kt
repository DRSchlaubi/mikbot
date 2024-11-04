package dev.schlaubi.mikbot.core.i18n.database.settings

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.stringChoice
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.SupportedLocales
import dev.schlaubi.mikbot.core.i18n.database.LangaugeUser
import dev.schlaubi.mikbot.core.i18n.database.LanguageDatabase
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.util.executableEverywhere
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.DatabaseI18nTranslations
import java.util.*

private class LanguageArguments : Arguments() {
    val language by stringChoice {
        name = DatabaseI18nTranslations.Commands.Language.Arguments.Language.name
        description = DatabaseI18nTranslations.Commands.Language.Arguments.Language.description

        choice(DatabaseI18nTranslations.Commands.Language.Arguments.Language.german, SupportedLocales.GERMAN.toLanguageTag())
        choice(DatabaseI18nTranslations.Commands.Language.Arguments.Language.english, SupportedLocales.ENGLISH.toLanguageTag())
        choice(DatabaseI18nTranslations.Commands.Language.Arguments.Language.italian, Locale.ITALIAN.toLanguageTag())
        choice(DatabaseI18nTranslations.Commands.Language.Arguments.Language.french, SupportedLocales.FRENCH.toLanguageTag())
    }
}

suspend fun SettingsModule.languageCommand() {
    ephemeralSlashCommand(::LanguageArguments) {
        name = DatabaseI18nTranslations.Commands.Language.name
        description = DatabaseI18nTranslations.Commands.Language.description
        executableEverywhere()

        action {
            val locale = Locale.forLanguageTag(arguments.language)

            val botUser = LanguageDatabase.collection.findOneById(user.id)
            val newUser = botUser?.copy(locale = locale) ?: LangaugeUser(user.id, locale)
            LanguageDatabase.collection.save(newUser)

            respond { content = translate(DatabaseI18nTranslations.Commands.Language.changed, arguments.language) }
        }
    }
}
