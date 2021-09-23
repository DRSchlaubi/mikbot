package dev.schlaubi.musicbot.config

import ch.qos.logback.classic.Level
import dev.schlaubi.envconf.EnvironmentVariable
import dev.schlaubi.envconf.getEnv
import dev.schlaubi.envconf.Config as EnvironmentConfig

object Config : EnvironmentConfig("") {
    val ENVIRONMENT: Environment by getEnvEnum(default = Environment.PRODUCTION)
    val LOG_LEVEL by getEnv(Level.INFO, Level::valueOf)
    val SENTRY_TOKEN by environment.optional()

    val DISCORD_TOKEN by environment
    val GAMES by getEnv(emptyList()) { it.split(",") }
    val MONGO_URL by environment
    val MONGO_DATABASE by environment

    val TEST_GUILD by getEnv { it.toLong() }.optional()
}

enum class Environment(val useSentry: Boolean) {
    PRODUCTION(true),
    DEVELOPMENT(false)
}

private inline fun <reified T : Enum<T>> getEnvEnum(
    prefix: String = "",
    default: T? = null
): EnvironmentVariable<T> =
    getEnv(prefix, default) { java.lang.Enum.valueOf(T::class.java, it) }
