package dev.schlaubi.mikbot.core.health

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.utils.loadModule
import dev.schlaubi.mikbot.core.health.check.HealthCheck
import dev.schlaubi.mikbot.core.health.ratelimit.setupDistributedRateLimiter
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.config.Environment
import dev.schlaubi.mikbot.plugin.api.getExtensions
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

    override fun ExtensionsBuilder.addExtensions() {
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
                setupDistributedRateLimiter()
            }
            applicationCommands {
                register = Config.POD_ID == (Config.TOTAL_SHARDS / Config.SHARDS_PER_POD)
                    || BotConfig.ENVIRONMENT == Environment.DEVELOPMENT
                    || !Config.ENABLE_SCALING
            }
        }
    }
}
