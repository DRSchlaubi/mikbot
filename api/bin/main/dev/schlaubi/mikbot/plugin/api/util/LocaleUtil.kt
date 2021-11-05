package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.GuildChannel
import java.util.*

/** Resolve the locale for this command context. **/
public suspend fun ExtensibleBot.getLocale(channel: GuildChannel, user: User): Locale {
    var locale: Locale? = null

    for (resolver in settings.i18nBuilder.localeResolvers) {
        val result = resolver(channel.guild, channel, user)

        if (result != null) {
            locale = result
            break
        }
    }

    return locale ?: settings.i18nBuilder.defaultLocale
}
