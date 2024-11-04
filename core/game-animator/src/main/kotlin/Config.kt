/* ktlint-disable package-name */
package dev.schlaubi.mikbot.core.game_animator

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    val GAMES by getEnv(emptyList()) { it.split(",").map(Game.Companion::parse) }
}
