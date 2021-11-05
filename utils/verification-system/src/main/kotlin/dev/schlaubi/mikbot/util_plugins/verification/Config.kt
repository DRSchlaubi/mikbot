package dev.schlaubi.mikbot.util_plugins.verification

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val VERIFY_SERVER_PORT by getEnv(8080) { it.toInt() }
    val VERIFY_SERVER_HOST by getEnv("127.0.0.1")
    val VERIFY_SERVER_URL by getEnv("http://127.0.0.1")
    val VERIFY_CLIENT_ID by environment.optional()
    val VERIFY_CLIENT_SECRET by environment.optional()
}
