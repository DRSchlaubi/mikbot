package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.Event

/**
 * Event fired when all shards are first connected to Discord.
 */
@OptIn(KordPreview::class)
public class AllShardsReadyEvent(
    override val kord: Kord,
    override val shard: Int,
    override val customContext: Any? = null,
) : Event
