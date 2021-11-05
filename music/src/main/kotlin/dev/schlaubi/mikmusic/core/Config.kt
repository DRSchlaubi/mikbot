package dev.schlaubi.mikmusic.core

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {

    val HAPPI_KEY by environment.optional()
    val YOUTUBE_API_KEY by environment
    val SPOTIFY_CLIENT_ID by getEnv("")
    val SPOTIFY_CLIENT_SECRET by getEnv("")
}
