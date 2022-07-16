package dev.schlaubi.mikbot.core.health.check

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.Kord
import kotlinx.coroutines.isActive
import org.koin.core.component.inject
import org.pf4j.Extension

@Extension
class KordHealthCheck : HealthCheck, KordExKoinComponent {

    private val kord by inject<Kord>()

    override suspend fun checkHealth(): Boolean = kord.gateway.gateways.all { it.value.isActive } && kord.isActive
}
