package dev.schlaubi.mikbot.util_plugins.ktor.api

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import io.ktor.http.*

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
    @InternalAPI
    val WEB_SERVER_URL by getEnv(Url("http://localhost:8080")) { Url(it) }
}
