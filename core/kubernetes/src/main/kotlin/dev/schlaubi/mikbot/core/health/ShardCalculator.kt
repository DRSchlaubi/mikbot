package dev.schlaubi.mikbot.core.health

import dev.kord.gateway.builder.Shards
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOG = KotlinLogging.logger { }

fun calculateShards(config: Config = Config, podId: Int = config.POD_ID): Shards {
    val totalShards = config.TOTAL_SHARDS
    val firstShard = config.SHARDS_PER_POD * podId
    val lastShard = (firstShard + (config.SHARDS_PER_POD - 1)).coerceAtMost(totalShards - 1)

    LOG.debug { "Determined shards for $podId ($firstShard..$lastShard)" }
    return Shards(totalShards, firstShard..lastShard)
}
