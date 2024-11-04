package dev.schlaubi.mikbot.core.health

import dev.kordex.core.koin.KordExKoinComponent
import dev.schlaubi.mikbot.core.health.check.HealthCheck
import dev.schlaubi.mikbot.core.health.routes.HealthRoutes
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.core.component.inject
import org.pf4j.Extension

@Extension
class KubernetesAPIServer : KtorExtensionPoint, KordExKoinComponent {
    private val checks by inject<List<HealthCheck>>()

    override fun Application.apply() {
        routing {
            get<HealthRoutes.Health> {
                if (checks.all { it.isSuccessful() }) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

private val logger = KotlinLogging.logger {}

private suspend inline fun HealthCheck.isSuccessful(): Boolean {
    logger.debug { "Running health check ${this::class.qualifiedName}" }
    return checkHealth()
}
