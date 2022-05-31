package dev.schlaubi.mikbot.core.redeploy_hook

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.core.redeploy_hook.api.RedeployExtensionPoint
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val client = HttpClient()

suspend fun OwnerModule.redeployCommand() = ephemeralSlashCommand {
    name = "redeploy"
    description = "redeploys the bot"
    bundle = "owners"

    ownerOnly()

    action {
        coroutineScope {
            pluginSystem.getExtensions<RedeployExtensionPoint>().forEach {
                @Suppress("ConvertLambdaToReference")
                launch {
                    it.beforeRedeploy()
                }
            }
        }

        val host = Config.REDEPLOY_HOST ?: return@action notAvailable()
        val response = client.get(host) {
            header("Redeploy-Token", Config.REDEPLOY_TOKEN)
        }.bodyAsText()

        if (response == "Hook rules were not satisfied.") {
            return@action notAvailable()
        }

        respond {
            content = translate("commands.redeploy.success")
        }
    }
}

private suspend fun EphemeralSlashCommandContext<*>.notAvailable() {
    respond {
        content = translate("command.redeploy.not_satisfied")
    }
}
