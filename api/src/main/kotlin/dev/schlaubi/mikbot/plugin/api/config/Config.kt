package dev.schlaubi.mikbot.plugin.api.config

import ch.qos.logback.classic.Level
import dev.kord.common.entity.Snowflake
import dev.schlaubi.envconf.EnvironmentVariable
import dev.schlaubi.envconf.getEnv
import java.nio.file.Path
import kotlin.io.path.Path
import dev.schlaubi.envconf.Config as EnvironmentConfig

public object Config : EnvironmentConfig("") {

    public val OWNER_GUILD: Snowflake? by getEnv { Snowflake(it) }.optional()
    public val BOT_OWNERS: List<Snowflake> by getEnv(emptyList()) { it.split(",").map { id -> Snowflake(id) } }
    public val ENVIRONMENT: Environment by getEnvEnum(default = Environment.PRODUCTION)
    public val LOG_LEVEL: Level by getEnv(Level.INFO, Level::valueOf)
    public val SENTRY_TOKEN: String? by environment.optional()

    public val DISCORD_TOKEN: String by environment
    public val MONGO_URL: String by environment
    public val MONGO_DATABASE: String by environment

    public val PLUGIN_PATH: Path by getEnv(Path("plugins")) { Path(it) }

    public val TEST_GUILD: Snowflake? by getEnv { Snowflake(it) }.optional()

    public val PLUGIN_REPOSITORIES: List<String> by getEnv(emptyList()) { it.split(",") }
    public val DOWNLOAD_PLUGINS: List<PluginSpec> by getEnv(emptyList()) {
        it.split(",").map { spec -> PluginSpec.parse(spec) }
    }
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
                PluginSpec(spec.substring(0, at), spec.substring(at, spec.length))
            } else {
                PluginSpec(spec, null)
            }
        }
    }
}
