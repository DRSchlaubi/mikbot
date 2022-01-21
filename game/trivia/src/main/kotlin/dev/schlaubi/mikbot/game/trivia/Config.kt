package dev.schlaubi.mikbot.game.trivia

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import io.ktor.util.*

object Config : EnvironmentConfig("") {
    @OptIn(InternalAPI::class)
    val GOOGLE_TRANSLATE_KEY by getEnv { it.decodeBase64Bytes() }
    val GOOGLE_TRANSLATE_PROJECT_ID by environment
    val GOOGLE_TRANSLATE_LOCATION by getEnv("global")
}
