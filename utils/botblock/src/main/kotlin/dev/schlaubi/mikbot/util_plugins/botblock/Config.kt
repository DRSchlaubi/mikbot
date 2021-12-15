package dev.schlaubi.mikbot.util_plugins.botblock

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val BOTBLOCK_DELAY by getEnv(10) { it.toInt() }
    val BOT_LIST_TOKENS by getEnv {
        it.split(",\\s*".toRegex()).associate {
            val (key, value) = it.split('=')

            key to value
        }
    }
}
