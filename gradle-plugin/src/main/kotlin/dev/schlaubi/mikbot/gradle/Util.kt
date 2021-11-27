package dev.schlaubi.mikbot.gradle

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import java.nio.file.Files
import java.nio.file.Path

fun readPluginsJson(path: Path): List<PluginInfo> = Files.readString(path).run { Json.decodeFromString(this) }

fun List<PluginInfo>.writeTo(path: Path) {
    Files.writeString(path, Json.encodeToString(this))
}

fun List<PluginInfo>.addPlugins(vararg plugins: PluginInfo): List<PluginInfo> {
    val existingPlugins = map { PluginWrapper(it) }
    val addedPlugins = plugins.map { PluginWrapper(it) }

    val (new, toUpdate) = addedPlugins.partition { it !in existingPlugins }
    val backlog = existingPlugins.filter { it !in addedPlugins }
    val map = existingPlugins.associateBy { (plugin) -> plugin.id }
    val updated = toUpdate.map { (plugin) ->
        val (parent) = map[plugin.id]!! // retrieve existing releases

        val newReleases = (parent.releases + plugin.releases).distinctBy { it.version }

        plugin.copy(releases = newReleases)
    }

    return (new + backlog).map { it.pluginInfo } + updated
}

val Project.pluginFilePath: String
    get() = "${name}/${version}/plugin-${name}-${version}.zip"

private data class PluginWrapper(val pluginInfo: PluginInfo) {
    override fun equals(other: Any?): Boolean = (other as? PluginWrapper)?.pluginInfo?.id == pluginInfo.id
    override fun hashCode(): Int = pluginInfo.id.hashCode()
}

fun Project.buildDependenciesString(): String {
    val plugin  = configurations.getByName("plugin")
    val optionalPlugin  = configurations.getByName("optionalPlugin")

    val required = plugin.allDependencies.map { it.toDependencyString() }
    val optional = optionalPlugin.allDependencies.map { it.toDependencyString(true) }

    return (required + optional).joinToString(", ")
}

fun Dependency.toDependencyString(optional: Boolean = false): String {
    val name = if (this is ProjectDependency) {
        dependencyProject.name
    } else {
        name
    }

    return "$name${if (optional) "?" else ""}@$version"
}
