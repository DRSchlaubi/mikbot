package dev.schlaubi.mikbot.gradle

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.readBytes

internal fun readPluginsJson(path: Path): List<PluginInfo> = Files.readString(path).run { Json.decodeFromString(this) }

internal fun List<PluginInfo>.writeTo(path: Path) {
    Files.writeString(path, Json.encodeToString(this))
}

internal fun List<PluginInfo>.addPlugins(vararg plugins: PluginInfo): List<PluginInfo> {
    val existingPlugins = map { PluginWrapper(it) }
    val addedPlugins = plugins.map { PluginWrapper(it) }

    val (new, toUpdate) = addedPlugins.partition { it !in existingPlugins }
    val backlog = existingPlugins.filter { it !in addedPlugins }
    val map = existingPlugins.associateBy { (plugin) -> plugin.id }
    val updated = toUpdate.map { (plugin) ->
        val (parent) = map[plugin.id]!! // retrieve existing releases

        val newReleases = (parent.releases + plugin.releases).distinctBy { it.version }

        if (newReleases.size == parent.releases.size) {
            parent // do not change existing sha512 checksums
        } else{
            plugin.copy(releases = newReleases)
        }
    }

    return (new + backlog).map { it.pluginInfo } + updated
}

internal val Project.pluginFilePath: String
    get() = "${name}/${version}/plugin-${pluginId}-${version}.zip"

private data class PluginWrapper(val pluginInfo: PluginInfo) {
    override fun equals(other: Any?): Boolean = (other as? PluginWrapper)?.pluginInfo?.id == pluginInfo.id
    override fun hashCode(): Int = pluginInfo.id.hashCode()
}

internal fun Project.buildDependenciesString(): String {
    val plugin = configurations.getByName("plugin")
    val optionalPlugin = configurations.getByName("optionalPlugin")

    val required = plugin.allDependencies.map { it.toDependencyString() }
    val optional = optionalPlugin.allDependencies.map { it.toDependencyString(true) }

    return (required + optional).joinToString(", ")
}

internal fun Dependency.toDependencyString(optional: Boolean = false): String {
    val name = if (this is ProjectDependency) {
        dependencyProject.pluginId
    } else {
        name.substringAfter("mikbot-")
    }

    return "$name${if (optional) "?" else ""}@$version"
}

/**
 * This removes the version part of Gradle artifacts and just returns the module name.
 */
fun File.removeVersion(): String? {
    val possibleVersions = name.split("-[0-9]".toRegex())
    if (possibleVersions.size <= 1) return null
    val version = possibleVersions.last()

    return name.substring(0, name.length - version.length - 2)
}

/**
 * Tries to configure this project for raw PF4J.
 */
fun Project.usePF4J() {
    extensions.configure<PluginExtension>(pluginExtensionName) {
        with(it) {
            ignoreDependencies.set(true)
            pluginMainFileLocation.set(
                buildDir.resolve("resources").resolve("main").resolve("plugin.properties").toPath()
            )
        }
    }
}

fun Path.sha512Checksum(): String = readBytes().sha512Checksum()

fun ByteArray.sha512Checksum(): String {
    val digest = MessageDigest.getInstance("SHA-512")
    val hashBytes = digest.digest(this)
    return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
}
