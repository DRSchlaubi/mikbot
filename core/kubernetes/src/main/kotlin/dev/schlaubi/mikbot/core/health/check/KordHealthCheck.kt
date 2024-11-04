package dev.schlaubi.mikbot.core.health.check

import dev.kord.core.Kord
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.extensions.event
import dev.kordex.core.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.isActive
import org.koin.core.component.inject
import org.pf4j.Extension
import dev.kordex.core.extensions.Extension as KordExtension

private val LOG = KotlinLogging.logger { }

/**
 * Whether the node is ready.
 */
internal var ready = false
    private set

@Extension
class KordHealthCheck : HealthCheck, KordExKoinComponent {

    private val kord by inject<Kord>()

    override suspend fun checkHealth(): Boolean =
        ready && kord.gateway.gateways.all { it.value.isActive } && kord.isActive

    override fun ExtensionsBuilder.addExtensions() {
        add(::ShardMonitor)
    }
}

private class ShardMonitor() : KordExtension() {
    override val name: String = "Shard monitor"

    override suspend fun setup() {
        event<AllShardsReadyEvent> {
            action {
                LOG.debug { "All shards are ready, returning 200 on health checks from now on" }
                ready = true
            }
        }
    }
}
