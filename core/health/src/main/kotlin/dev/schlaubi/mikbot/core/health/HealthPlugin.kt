package dev.schlaubi.mikbot.core.health

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.schlaubi.mikbot.core.health.check.HealthCheck
import dev.schlaubi.mikbot.plugin.api.*
import mu.KotlinLogging

@PluginMain
class HealthPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    private val healthChecks by lazy<List<HealthCheck>>(pluginSystem::getExtensions)
    private val logger = KotlinLogging.logger(log)
    override fun start() {
        logger.info { "Registered ${healthChecks.size} health checks available at /healthz" }
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            beforeKoinSetup {
                loadModule {
                    single { healthChecks }
                }
            }
        }
    }
}
