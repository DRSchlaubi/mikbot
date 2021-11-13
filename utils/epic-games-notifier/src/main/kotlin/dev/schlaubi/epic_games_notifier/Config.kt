package dev.schlaubi.epic_games_notifier

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val COUNTRY_CODE by environment
    val DISCORD_CLIENT_ID by environment
    val DISCORD_CLIENT_SECRET by environment
}
