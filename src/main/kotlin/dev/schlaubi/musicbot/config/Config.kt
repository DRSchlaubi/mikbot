package dev.schlaubi.musicbot.config

import ch.qos.logback.classic.Level
import dev.kord.common.entity.Snowflake
import dev.schlaubi.envconf.EnvironmentVariable
import dev.schlaubi.envconf.getEnv
import dev.schlaubi.envconf.Config as EnvironmentConfig

object Config : EnvironmentConfig("") {
    val OWNER_GUILD by getEnv { Snowflake(it) }.optional()
    val BOT_OWNERS by getEnv(emptyList()) { it.split(",").map { id -> Snowflake(id) } }
    val REDEPLOY_HOST by environment.optional()
    val REDEPLOY_TOKEN by environment.optional()
    val YOUTUBE_API_KEY by environment
    val ENVIRONMENT: Environment by getEnvEnum(default = Environment.PRODUCTION)
    val LOG_LEVEL by getEnv(Level.INFO, Level::valueOf)
    val SENTRY_TOKEN by environment.optional()

    val DISCORD_TOKEN by environment
    val GAMES by getEnv(emptyList()) { it.split(",") }
    val MONGO_URL by environment
    val MONGO_DATABASE by environment

    val TEST_GUILD by getEnv { it.toLong() }.optional()

    val SPOTIFY_CLIENT_ID by getEnv("")
    val SPOTIFY_CLIENT_SECRET by getEnv("")

    val VERIFIED_MODE by getEnv(false) { it.toBooleanStrict() }

    val VERIFY_SERVER_PORT by getEnv(8080) { it.toInt() }
    val VERIFY_SERVER_HOST by getEnv("127.0.0.1")
    val VERIFY_SERVER_URL by getEnv("http://127.0.0.1")
    val VERIFY_CLIENT_ID by environment.optional()
    val VERIFY_CLIENT_SECRET by environment.optional()
}

@Suppress("unused")
enum class Environment(val useSentry: Boolean) {
    PRODUCTION(true),
    DEVELOPMENT(false)
}

private inline fun <reified T : Enum<T>> getEnvEnum(
    prefix: String = "",
    default: T? = null
): EnvironmentVariable<T> =
    getEnv(prefix, default) { java.lang.Enum.valueOf(T::class.java, it) }
