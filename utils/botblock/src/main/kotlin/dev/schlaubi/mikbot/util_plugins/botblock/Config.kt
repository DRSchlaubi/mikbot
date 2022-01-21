package dev.schlaubi.mikbot.util_plugins.botblock

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val BOTBLOCK_DELAY by getEnv(10) { it.toInt() }
    val SUPPORTED_BOT_LISTS by getEnv {
        it.split(",\\s*".toRegex()).map { listName ->
            it to buildString(listName.length) {
                listName.forEach { char ->
                    if (char.isLetterOrDigit()) append(char.uppercaseChar()) else append('_')
                }
            }
        }
    }
}
