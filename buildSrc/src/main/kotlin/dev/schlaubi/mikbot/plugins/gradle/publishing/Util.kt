package dev.schlaubi.mikbot.plugins.gradle.publishing

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import java.nio.file.Files
import java.nio.file.Path

fun readPluginsJson(path: Path): List<PluginInfo> = Files.readString(path).run { Json.decodeFromString(this) }

fun List<PluginInfo>.writeTo(path: Path) {
    Files.writeString(path, Json.encodeToString(this))
}

fun List<PluginInfo>.addPlugins(vararg plugins: PluginInfo): List<PluginInfo> {
    val (new, toUpdate) = plugins.partition { it !in this }
    val backlog = filter { it !in plugins }
    val map = associateBy { it.id }
    val updated = toUpdate.map {
        val parent = map[it.id]!! // retrieve existing releases

        it.copy(releases = parent.releases + it.releases)
    }

    return new + backlog + updated
}

val Project.pluginFilePath: String
    get() = "${name}/${version}/plugin-${name}-${version}.zip"
