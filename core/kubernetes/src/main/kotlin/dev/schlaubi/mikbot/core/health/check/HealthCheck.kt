package dev.schlaubi.mikbot.core.health.check

import dev.kordex.core.builders.ExtensionsBuilder
import org.pf4j.ExtensionPoint

interface HealthCheck : ExtensionPoint {
    /**
     * Runs the health check.
     *
     * @return if the health check succeeded
     */
    suspend fun checkHealth(): Boolean

    /**
     * Register an optional extension.
     */
    fun ExtensionsBuilder.addExtensions() = Unit
}
