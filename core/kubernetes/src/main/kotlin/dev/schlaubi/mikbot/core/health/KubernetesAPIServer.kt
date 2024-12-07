package dev.schlaubi.mikbot.core.health

import dev.schlaubi.mikbot.core.health.check.HealthCheck
import dev.schlaubi.mikbot.core.health.routes.HealthRoutes
import dev.schlaubi.mikbot.core.redeploy_hook.api.RedeployExtensionPoint
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.getExtensions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

fun startServer(checks: List<HealthCheck>, context: PluginContext) =
    embeddedServer(Netty, Config.PORT) {
        install(Resources)

        routing {
            get<HealthRoutes.Health> {
                if (checks.all { it.isSuccessful() }) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            if (context.pluginWrapper.pluginManager.getPlugin("redeploy-hook") != null) {
                val redeployHooks = context.pluginSystem.getExtensions<RedeployExtensionPoint>()
                get<HealthRoutes.PreStop> {
                    redeployHooks.forEach { it.beforeRedeploy() }
                }
            }
        }
    }.start(wait = false)

private val logger = KotlinLogging.logger {}

private suspend inline fun HealthCheck.isSuccessful(): Boolean {
    logger.debug { "Running health check ${this::class.qualifiedName}" }
    return checkHealth()
}
