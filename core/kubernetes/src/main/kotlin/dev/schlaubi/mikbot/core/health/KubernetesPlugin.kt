package dev.schlaubi.mikbot.core.health

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.schlaubi.mikbot.core.health.check.HealthCheck
import dev.schlaubi.mikbot.plugin.api.*
import dev.schlaubi.mikbot.plugin.api.config.Environment
import mu.KotlinLogging
import dev.schlaubi.mikbot.plugin.api.config.Config as BotConfig

private val LOG = KotlinLogging.logger { }

@PluginMain
class KubernetesPlugin(context: PluginContext) : Plugin(context) {
    private val healthChecks by lazy<List<HealthCheck>>(context.pluginSystem::getExtensions)
    private val logger = KotlinLogging.logger(log)
    override fun start() {
        logger.info { "Registered ${healthChecks.size} health checks available at /healthz" }
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        if (Config.ENABLE_SCALING) {
            add(::RebalancerExtension)
        }
        healthChecks.forEach {
            with(it) {
                addExtensions()
            }
        }
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            beforeKoinSetup {
                loadModule {
                    single { healthChecks }
                }
            }
        }

        if (Config.ENABLE_SCALING) {
            LOG.debug { "Scaling is enabled " }
            kord {
                sharding { calculateShards() }
            }
            applicationCommands {
                register = Config.POD_ID == 0
                    || BotConfig.ENVIRONMENT == Environment.DEVELOPMENT
                    || !Config.ENABLE_SCALING
            }
        }
    }
}
