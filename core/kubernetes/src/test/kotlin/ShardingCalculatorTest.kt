package dev.schlaubi.mikbot.core.kubernetes.test

import dev.kord.gateway.builder.Shards
import dev.schlaubi.mikbot.core.health.calculateShards
import kotlin.test.Test
import kotlin.test.assertTrue

class ShardingCalculatorTest {
    @Test
    fun `test correct shard config for even shard count`() {
        val totalShards = 4
        assertSameShardCount(Shards(4, 0..1), calculateShards(totalShards = totalShards, podId = 0))
        assertSameShardCount(Shards(4, 2..3), calculateShards(totalShards = totalShards, podId = 1))
    }


    @Test
    fun `test correct shard config for uneven shard count`() {
        val totalShards = 5

        assertSameShardCount(Shards(5, 0..1), calculateShards(totalShards = totalShards, podId = 0))
        assertSameShardCount(Shards(5, 2..3), calculateShards(totalShards = totalShards, podId = 1))
        assertSameShardCount(Shards(5, listOf(4)), calculateShards(totalShards = totalShards, podId = 2))
    }

    @Test
    fun `test correct shard config for one shard per Pod`() {
        val totalShards = 3
        val shardsPerPod = 1

        assertSameShardCount(
            Shards(totalShards, listOf(0)),
            calculateShards(totalShards = totalShards, shardsPerPod = shardsPerPod, podId = 0)
        )
        assertSameShardCount(
            Shards(totalShards, listOf(1)),
            calculateShards(totalShards = totalShards, shardsPerPod = shardsPerPod, podId = 1)
        )
        assertSameShardCount(
            Shards(totalShards, listOf(2)),
            calculateShards(totalShards = totalShards, shardsPerPod = shardsPerPod, podId = 2)
        )
    }
}

private fun assertSameShardCount(expected: Shards, actual: Shards) {
    assertTrue(
        expected.totalShards == actual.totalShards &&
            expected.indices.toList() == actual.indices.toList(),
        "Expected <$expected>, actual <$actual>."
    )
}
