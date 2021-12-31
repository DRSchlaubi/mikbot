package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.Kord
import dev.kord.core.event.Event
import kotlin.coroutines.CoroutineContext

/**
 * Event fired when all shards are first connected to Discord.
 */
public class AllShardsReadyEvent(
    override val kord: Kord,
    override val shard: Int,
    override val coroutineContext: CoroutineContext
) : Event
