package dev.schlaubi.mikmusic.core

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val ENABLE_MUSIC_CHANNEL_FEATURE: Boolean by getEnv(true, String::toBooleanStrict)
    val HAPPI_KEY by getEnv().optional()
    val YOUTUBE_API_KEY by this
    val SPOTIFY_CLIENT_ID by getEnv("")
    val SPOTIFY_CLIENT_SECRET by getEnv("")
    val IMAGE_COLOR_SERVICE_URL by getEnv().optional()
    val DEFAULT_SEARCH_PROVIDER by getEnv("ytsearch")
}
