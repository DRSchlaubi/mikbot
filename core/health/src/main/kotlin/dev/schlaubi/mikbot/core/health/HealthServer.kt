package dev.schlaubi.mikbot.core.health

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
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
class HealthServer : KtorExtensionPoint, KordExKoinComponent {
    private val checks by inject<List<HealthCheck>>()
    private val logger = KotlinLogging.logger {}

    override fun Application.apply() {
        routing {
            get<HealthRoutes.Health> {
                var success = true
                for (check in checks) {
                    logger.debug { "Running health check ${check::class.qualifiedName}" }
                    if (!check.checkHealth()) {
                        success = false
                    }
                }
                if (!success) {
                    call.respond(HttpStatusCode.InternalServerError)
                } else {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
