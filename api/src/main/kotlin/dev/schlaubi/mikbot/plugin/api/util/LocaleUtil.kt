package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.Locale
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.GuildChannel
import java.util.Locale as JLocale

/** Resolve the locale for this command context. **/
public suspend fun ExtensibleBot.getLocale(channel: GuildChannel, user: User): JLocale {
    var locale: JLocale? = null

    for (resolver in settings.i18nBuilder.localeResolvers) {
        val result = resolver(channel.guild, channel, user)

        if (result != null) {
            locale = result
            break
        }
    }

    return locale ?: settings.i18nBuilder.defaultLocale
}

/**
 * This converts the language codes used by Discord (e.g `de`) to the ones used by [JLocale] like `de_DE`.
 *
 * If [Locale.country] is already specified, it will just use the already specified version
 */
public fun Locale.convertToISO(): Locale = when {
    !country.isNullOrBlank() -> this
    language == "cs" -> copy(country = "CZ")
    language == "da" -> copy(country = "DK")
    language == "el" -> copy(country = "GR")
    language == "hi" -> copy(country = "IN")
    language == "ja" -> copy(country = "JP")
    language == "uk" -> copy(country = "UA")
    language == "vi" -> copy(country = "VN")
    else -> Locale(language, language.uppercase(JLocale.ENGLISH))
}
