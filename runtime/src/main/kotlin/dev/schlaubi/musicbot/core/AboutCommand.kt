package dev.schlaubi.musicbot.core

import dev.kordex.core.builders.AboutBuilder
import dev.kordex.core.builders.about.CopyrightType
import dev.schlaubi.mikbot.plugin.api.AboutExtensionPoint
import dev.schlaubi.mikbot.plugin.api.getExtensions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.pf4j.PluginState
import java.io.InputStream

@Serializable
private data class LicenseReport(
    val dependencies: List<Dependency> = emptyList(),
) {
    @Suppress("unused")
    @Serializable
    class Dependency(
        val moduleName: String,
        val moduleUrl: String? = null,
        val moduleVersion: String,
        val moduleLicense: String? = null,
        val moduleLicenseUrl: String? = null,
    )
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun AboutBuilder.aboutCommand(bot: Bot) {
    copyright("MikBot", "MIT", CopyrightType.Framework, "https://github.com/DRSchlaubi/mikbot")

    bot.pluginLoader.plugins.asSequence()
        .filter { it.pluginState != PluginState.DISABLED }
        .mapNotNull { it.pluginClassLoader.getResourceAsStream("license-report.json") }
        .map { it.use<InputStream, LicenseReport>(Json.Default::decodeFromStream) }
        .flatMap(LicenseReport::dependencies)
        .distinctBy(LicenseReport.Dependency::moduleName)
        .forEach {
            copyright(it.moduleName, it.moduleLicense ?: "Unknown License", CopyrightType.Library, it.moduleUrl)
        }

    bot.pluginSystem.getExtensions<AboutExtensionPoint>().forEach {
        with(it) { apply() }
    }
}
