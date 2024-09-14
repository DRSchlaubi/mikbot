package dev.schlaubi.mikbot.core.health

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.reidsync.kxjsonpatch.generatePatch
import dev.kord.common.KordConstants
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.rest.json.response.BotGatewayResponse
import dev.kord.rest.route.Route
import dev.schlaubi.mikbot.core.health.check.ready
import dev.schlaubi.mikbot.core.health.util.blocking
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.discordError
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpHeaders.UserAgent
import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1StatefulSet
import io.kubernetes.client.util.PatchUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import io.kubernetes.client.util.Config as KubeConfig

private val startup = TimeSource.Monotonic.markNow()

private class RebalanceArguments() : Arguments() {
    val forceTo by optionalInt {
        name = "force_to"
        description = "commands.rebalance.arguments.force_to.description"
    }
}

class RebalancerExtension(context: PluginContext) : MikBotModule(context) {
    override val name: String = "rebalancer"
    override val bundle: String = "kubernetes"

    private val kubeClient = KubeConfig.defaultClient()
    private val kubeApi = AppsV1Api(kubeClient)

    override suspend fun setup() {
        command()

        // Only register self-update on the first pod
        if (Config.POD_ID == 0) {
            eventListener()
        }
    }

    private suspend fun eventListener() = event<GuildCreateEvent> {
        check {
            failIfNot(ready)
            // Do not call listener when pod just started
            // this is to prevent initial guild_creates to cause this
            failIf(startup.elapsedNow() > 5.minutes)
        }
        action {
            val (_, newTotalShards) = getGatewayInfo()
            if (newTotalShards > Config.TOTAL_SHARDS) {
                reBalance(newTotalShards)
            }
        }
    }

    private suspend fun command() = ephemeralSlashCommand(::RebalanceArguments) {
        name = "rebalance"
        description = "commands.rebalance.description"
        ownerOnly()

        action {
            val (_, newTotalShards) = getGatewayInfo()
            if (arguments.forceTo != null) {
                reBalance(arguments.forceTo!!)
            } else if (newTotalShards > Config.TOTAL_SHARDS) {
                reBalance(newTotalShards)
            } else if (arguments.forceTo != null) {
                reBalance(arguments.forceTo!!)
            } else {
                discordError(translate("commands.rebalance.already_balanced"))
            }

            respond {
                content = translate("commands.rebalance.done")
            }
        }

    }

    private suspend fun getGatewayInfo(): BotGatewayResponse {
        val response = kord.resources.httpClient.get("${Route.baseUrl}${Route.GatewayBotGet.path}") {
            header(UserAgent, KordConstants.USER_AGENT)
            header(Authorization, "Bot ${kord.resources.token}")
        }

        return Json.decodeFromString(response.bodyAsText())
    }

    private suspend fun reBalance(newTotalShards: Int) {
        val json =
            generateStatefulSetSpec(replicas = ceil(newTotalShards.toDouble() / Config.SHARDS_PER_POD.toDouble()).toInt(), totalShards = newTotalShards)

        val jsonPatch = generateStatefulSetSpec().generatePatch(json)

        val patch = V1Patch(Json.encodeToString(jsonPatch))

        blocking {
            PatchUtils.patch(V1StatefulSet::class.java, {
                kubeApi.patchNamespacedStatefulSet(Config.STATEFUL_SET_NAME, Config.NAMESPACE, patch)
                    .buildCall(null)
            }, "application/json-patch+json", kubeClient)
        }
    }
}

private fun generateStatefulSetSpec(replicas: Int = 0, totalShards: Int = 0) = buildJsonObject {
    putJsonObject("spec") {
        put("replicas", replicas)

        putJsonObject("template") {
            putJsonObject("spec") {
                putJsonArray("containers") {
                    addJsonObject {
                        put("name", Config.CONTAINER_NAME)
                        putJsonArray("env") {
                            addJsonObject {
                                put("name", "TOTAL_SHARDS")
                                put("value", totalShards.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}
