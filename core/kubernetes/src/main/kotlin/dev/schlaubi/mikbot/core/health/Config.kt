package dev.schlaubi.mikbot.core.health

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig() {
    val ENABLE_SCALING by getEnv(false, String::toBooleanStrict)
    val POD_ID by getEnv(transform = String::toInt)
    val SHARDS_PER_POD by getEnv(2, String::toInt)
    val TOTAL_SHARDS by getEnv(transform = String::toInt)
    val KUBERNETES_PORT by getEnv(8081, String::toInt)
    val STATEFUL_SET_NAME by this
    val NAMESPACE by getEnv("default")
    val CONTAINER_NAME by this
    val REDIS_URL by this
}
