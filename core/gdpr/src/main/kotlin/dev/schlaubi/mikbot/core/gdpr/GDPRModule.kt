package dev.schlaubi.mikbot.core.gdpr

import dev.kord.rest.builder.component.ActionRowBuilder
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import kotlinx.coroutines.flow.first

class GDPRModule : SubCommandModule() {
    override val name: String = "gdpr"
    override val bundle: String = "gdpr"
    override val commandName: String = "gdpr"

    val dataPoints: List<DataPoint> =
        pluginSystem.getExtensions<GDPRExtensionPoint>().flatMap { it.provideDataPoints() } + listOf(
            UserIdDataPoint,
            SentryDataPoint
        )

    val interactiveDataPoints = dataPoints.filterIsInstance<PermanentlyStoredDataPoint>()

    override suspend fun overrideSetup() {
        infoCommand()
        requestCommand()
        deleteCommand()
    }

    @Suppress("UNREACHABLE_CODE")
    suspend fun ActionRowBuilder.clickCommandButton(name: String, label: String) {
        // https://github.com/discord/discord-api-docs/discussions/3347#discussioncomment-1162191
        return
        val command = kord.globalCommands.first { it.name == name }

        val url = "discord://commands/${kord.resources.applicationId}/${command.id}"

        linkButton(url) {
            this.label = label
        }
    }
}
