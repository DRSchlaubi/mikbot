package dev.schlaubi.mikbot.core.health

import dev.kord.gateway.builder.Shards
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOG = KotlinLogging.logger { }

fun calculateShards(shardsPerPod: Int = Config.SHARDS_PER_POD, totalShards: Int = Config.TOTAL_SHARDS, podId: Int = Config.POD_ID): Shards {
    val firstShard = shardsPerPod * podId
    val lastShard = (firstShard + (shardsPerPod - 1)).coerceAtMost(totalShards - 1)

    LOG.debug { "Determined shards for $podId ($firstShard..$lastShard)" }
    return Shards(totalShards, firstShard..lastShard)
}
