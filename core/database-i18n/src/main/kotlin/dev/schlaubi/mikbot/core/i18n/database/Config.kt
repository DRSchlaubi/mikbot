package dev.schlaubi.mikbot.core.i18n.database

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import java.util.*

object Config : EnvironmentConfig("") {
    val DEFAULT_LOCALE by getEnv { Locale.forLanguageTag(it) }
}
