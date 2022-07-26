package dev.schlaubi.mikbot.util_plugins.verification

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val DISCORD_CLIENT_ID by environment.optional()
    val DISCORD_CLIENT_SECRET by environment.optional()

    val DISCORD_INVITE_PERMISSION: Long by getEnv(328576854080, String::toLong)
}
