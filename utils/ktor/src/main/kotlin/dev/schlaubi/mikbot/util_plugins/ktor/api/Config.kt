package dev.schlaubi.mikbot.util_plugins.ktor.api

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

/**
 * Configuration of the Ktor web server.
 */
object Config : EnvironmentConfig("") {
    /**
     * The port of the web server.
     */
    val WEB_SERVER_PORT by getEnv(8080) { it.toInt() }

    /**
     * The host of the web server.
     */
    val WEB_SERVER_HOST by getEnv("127.0.0.1")

    /**
     * The web server url
     */
    val WEB_SERVER_URL by getEnv("http://127.0.0.1")
}
