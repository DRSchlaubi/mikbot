package dev.schlaubi.mikbot.core.redeploy_hook

import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.core.redeploy_hook.api.RedeployExtensionPoint
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import dev.schlaubi.mikbot.plugin.api.owner.ownerOnly
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.RedeployHookTranslations
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val client = HttpClient()

suspend fun OwnerModule.redeployCommand() = ephemeralSlashCommand {
    name = RedeployHookTranslations.Commands.Redeploy.name
    description = RedeployHookTranslations.Commands.Redeploy.description

    ownerOnly()

    action {
        coroutineScope {
            context.pluginSystem.getExtensions<RedeployExtensionPoint>().forEach {
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
            content = translate(RedeployHookTranslations.Commands.Redeploy.success)
        }
    }
}

private suspend fun EphemeralSlashCommandContext<*, *>.notAvailable() {
    respond {
        content = translate(RedeployHookTranslations.Commands.Redeploy.not_satisfied)
    }
}
