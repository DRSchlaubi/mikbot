package dev.schlaubi.mikbot.core.redeploy_hook

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.core.redeploy_hook.api.RedeployExtensionPoint
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header

private val client = HttpClient()

suspend fun OwnerModule.redeployCommand() = ephemeralSlashCommand {
    name = "redeploy"
    description = "redeploys the bot"

    ownerOnly()

    action {
        pluginSystem.getExtensions<RedeployExtensionPoint>().forEach {
            it.beforeRedeploy()
        }

        val host = Config.REDEPLOY_HOST ?: return@action notAvailable()
        val response = client.get<String>(host) {
            header("Redeploy-Token", Config.REDEPLOY_TOKEN)
        }

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
