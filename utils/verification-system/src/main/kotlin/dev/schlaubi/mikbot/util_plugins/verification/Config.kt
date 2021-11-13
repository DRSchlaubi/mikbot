package dev.schlaubi.mikbot.util_plugins.verification

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val VERIFY_CLIENT_ID by environment.optional()
    val VERIFY_CLIENT_SECRET by environment.optional()
}
