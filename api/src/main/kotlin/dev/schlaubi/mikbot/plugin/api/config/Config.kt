package dev.schlaubi.mikbot.plugin.api.config

import ch.qos.logback.classic.Level
import dev.kord.common.entity.Snowflake
import dev.schlaubi.envconf.EnvironmentVariable
import dev.schlaubi.envconf.getEnv
import java.nio.file.Path
import kotlin.io.path.Path
import dev.schlaubi.envconf.Config as EnvironmentConfig

/**
 * Default Configuration options for the bot.
 */
public object Config : EnvironmentConfig("") {

    /**
     * The guild used for owner module commands.
     */
    public val OWNER_GUILD: Snowflake? by getEnv { Snowflake(it) }.optional()

    /**
     * A list of bot owners.
     */
    public val BOT_OWNERS: List<Snowflake> by getEnv(emptyList()) { it.split(",").map { id -> Snowflake(id) } }

    /**
     * The Environment the bot runs in
     *
     * @see Environment
     */
    public val ENVIRONMENT: Environment by getEnvEnum(default = Environment.PRODUCTION)

    /**
     * The [LOG_LEVEL] of the bot
     *
     * @see Level
     */
    public val LOG_LEVEL: Level by getEnv(Level.INFO, Level::valueOf)

    /**
     * The Sentry token.
     */
    public val SENTRY_TOKEN: String? by environment.optional()

    /**
     * The Discord token.
     */
    public val DISCORD_TOKEN: String by environment

    /**
     * The [Mongo connection String](https://docs.mongodb.com/manual/reference/connection-string/) used for the bots Database features.
     */
    public val MONGO_URL: String? by environment.optional()

    /**
     * The database to use.
     *
     * **This has to be specified, even if there is a database in [MONGO_URL]**
     */
    public val MONGO_DATABASE: String? by environment.optional()

    /**
     * The path to the plugins folder.
     */
    public val PLUGIN_PATH: Path by getEnv(Path("plugins")) { Path(it) }

    /**
     * If you set this variable, all commands will be only registered for this guild, only use this in testing.
     */
    public val TEST_GUILD: Snowflake? by getEnv { Snowflake(it) }.optional()

    /**
     * A list of plugin repositories.
     */
    public val PLUGIN_REPOSITORIES: List<String> by getEnv(listOf("https://storage.googleapis.com/mikbot-plugins/")) {
        it.split(
            ","
        )
    }

    /**
     * The bot will try and download all plugins in this list from [PLUGIN_REPOSITORIES] on startup.
     *
     * **This doesn't download depnendency plugins**
     */
    public val DOWNLOAD_PLUGINS: List<PluginSpec> by getEnv(emptyList()) {
        it.split(",").map { spec -> PluginSpec.parse(spec) }
    }

    /**
     * Whether the bot will try to update the plugins or not.
     */
    public val UPDATE_PLUGINS: Boolean by getEnv(true, String::toBooleanStrict)

    public val VALIDATE_CHECKSUMS: Boolean by getEnv(true, String::toBooleanStrict)
}

@Suppress("unused")
public enum class Environment(public val useSentry: Boolean) {
    PRODUCTION(true),
    DEVELOPMENT(false)
}

private inline fun <reified T : Enum<T>> getEnvEnum(
    prefix: String = "",
    default: T? = null
): EnvironmentVariable<T> =
    getEnv(prefix, default) { java.lang.Enum.valueOf(T::class.java, it) }

public data class PluginSpec(public val id: String, public val version: String?) {
    public companion object {
        public fun parse(spec: String): PluginSpec {
            val at = spec.indexOf('@')
            return if (at >= 0) {
                PluginSpec(spec.substring(0, at), spec.substring(at + 1, spec.length))
            } else {
                PluginSpec(spec, null)
            }
        }
    }
}
