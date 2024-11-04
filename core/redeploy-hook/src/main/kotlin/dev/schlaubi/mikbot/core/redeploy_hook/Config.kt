package dev.schlaubi.mikbot.core.redeploy_hook

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val REDEPLOY_HOST by getEnv().optional()
    val REDEPLOY_TOKEN by getEnv().optional()
}
