package dev.schlaubi.mikbot.core.redeploy_hook

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    public val REDEPLOY_HOST by environment.optional()
    public val REDEPLOY_TOKEN by environment.optional()
}
