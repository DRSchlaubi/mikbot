package dev.schlaubi.mikmusic.lyrics

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig() {
    val LYRICS_WEB_URL by getEnv("http://localhost:3001")
}
