package dev.schlaubi.mikbot.core.gdpr

import dev.kordex.core.commands.application.slash.SlashCommand
import dev.schlaubi.mikbot.core.gdpr.api.DataPoint
import dev.schlaubi.mikbot.core.gdpr.api.GDPRExtensionPoint
import dev.schlaubi.mikbot.core.gdpr.api.PermanentlyStoredDataPoint
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.plugin.api.module.SubCommandModule
import dev.schlaubi.mikbot.plugin.api.util.executableEverywhere
import dev.schlaubi.mikbot.translations.GdprTranslations

class GDPRModule(context: PluginContext) : SubCommandModule(context) {
    override val name: String = "gdpr"
    override val commandName = GdprTranslations.Commands.Gdpr.name

    val dataPoints: List<DataPoint> =
        context.pluginSystem.getExtensions<GDPRExtensionPoint>()
            .flatMap(GDPRExtensionPoint::provideDataPoints) + listOf(
            UserIdDataPoint,
            SentryDataPoint
        )

    val interactiveDataPoints = dataPoints.filterIsInstance<PermanentlyStoredDataPoint>()

    override fun SlashCommand<*, *, *>.commandSettings() {
        executableEverywhere()
    }

    override suspend fun overrideSetup() {
        infoCommand()
        requestCommand()
        deleteCommand()
    }
}
