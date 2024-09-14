package dev.schlaubi.mikbot.core.kubernetes.test

import dev.kord.gateway.builder.Shards
import dev.schlaubi.mikbot.core.health.Config
import dev.schlaubi.mikbot.core.health.calculateShards
import org.junitpioneer.jupiter.SetEnvironmentVariable
import kotlin.test.Test
import kotlin.test.assertTrue

class ShardingCalculatorTest {
    @SetEnvironmentVariable(key = "TOTAL_SHARDS", value = "4")
    @Test
    fun `test correct shard config for even shard count`() {
        val config = Config()

        assertSameShardCount(Shards(4, 0..1), calculateShards(config, podId = 0))
        assertSameShardCount(Shards(4, 2..3), calculateShards(config, podId = 1))
    }


    @SetEnvironmentVariable(key = "TOTAL_SHARDS", value = "5")
    @Test
    fun `test correct shard config for uneven shard count`() {
        val config = Config()
        assertSameShardCount(Shards(5, 0..1), calculateShards(config, podId = 0))
        assertSameShardCount(Shards(5, 2..3), calculateShards(config, podId = 1))
        assertSameShardCount(Shards(5, listOf(4)), calculateShards(config, podId = 2))
    }

    @SetEnvironmentVariable(key = "TOTAL_SHARDS", value = "3")
    @SetEnvironmentVariable(key = "SHARDS_PER_POD", value = "1")
    @Test
    fun `test correct shard config for one shard per Pod`() {
        val config = Config()
        val totalShards = 3

        assertSameShardCount(Shards(totalShards, listOf(0)), calculateShards(config, podId = 0))
        assertSameShardCount(Shards(totalShards, listOf(1)), calculateShards(config, podId = 1))
        assertSameShardCount(Shards(totalShards, listOf(2)), calculateShards(config, podId = 2))
    }
}

private fun assertSameShardCount(expected: Shards, actual: Shards) {
    assertTrue(
        expected.totalShards == actual.totalShards &&
            expected.indices.toList() == actual.indices.toList(),
        "Expected <$expected>, actual <$actual>."
    )
}
