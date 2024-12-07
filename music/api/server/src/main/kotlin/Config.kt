package dev.schlaubi.mikmusic.api

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig() {
    val DISCORD_CLIENT_ID by this
    val DISCORD_CLIENT_SECRET by this
    val JWT_SECRET by getEnv("spukyscaryskeletons")
}
