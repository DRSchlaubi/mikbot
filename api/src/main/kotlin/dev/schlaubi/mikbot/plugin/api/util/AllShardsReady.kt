package dev.schlaubi.mikbot.plugin.api.util

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@InternalAPI
public val readyEventFlow: MutableSharedFlow<AllShardsReadyEvent> = MutableSharedFlow()

public class AllShardsReadyEvent(
    override val kord: Kord,
    override val shard: Int,
    override val coroutineContext: CoroutineContext
) : Event {
    public companion object {
        @OptIn(InternalAPI::class)
        public fun listen(coroutineScope: CoroutineScope, listener: suspend AllShardsReadyEvent.() -> Unit): Job = readyEventFlow.onEach {

        }.launchIn(coroutineScope)
    }
}
