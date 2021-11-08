package dev.schlaubi.mikbot.core.i18n.database

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import java.util.*

object Config : EnvironmentConfig("") {
    val DEFAULT_LOCALE by getEnv(SupportedLocales.ENGLISH) { Locale.forLanguageTag(it) }
}
