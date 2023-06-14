package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.PluginExtension
import dev.schlaubi.mikbot.gradle.extension.pluginExtensionName
import dev.schlaubi.mikbot.gradle.extension.pluginId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.readBytes

internal fun readPluginsJson(path: Path): List<PluginInfo> = Files.readString(path).run { Json.decodeFromString(this) }

internal fun List<PluginInfo>.writeTo(path: Path) {
    Files.writeString(path, Json.encodeToString(this))
}

internal fun List<PluginInfo>.addPlugins(vararg plugins: PluginInfo): List<PluginInfo> {
    val existingPlugins = map(::PluginWrapper)
    val addedPlugins = plugins.map(::PluginWrapper)

    val (new, toUpdate) = addedPlugins.partition { it !in existingPlugins }
    val backlog = existingPlugins.filter { it !in addedPlugins }
    val map = existingPlugins.associateBy { (plugin) -> plugin.id }
    val updated = toUpdate.map { (plugin) ->
        val (parent) = map.getValue(plugin.id) // retrieve existing releases

        val newReleases = (parent.releases + plugin.releases).distinctBy(PluginRelease::version)

        if (newReleases.size == parent.releases.size) {
            parent // do not change existing sha512 checksums
        } else {
            plugin.copy(releases = newReleases)
        }
    }

    return (new + backlog).map(PluginWrapper::pluginInfo) + updated
}

internal val Project.pluginFilePath: String
    get() = "${pluginId}/${version}/plugin-${pluginId}-${version}.zip"

private data class PluginWrapper(val pluginInfo: PluginInfo) {
    override fun equals(other: Any?): Boolean = (other as? PluginWrapper)?.pluginInfo?.id == pluginInfo.id
    override fun hashCode(): Int = pluginInfo.id.hashCode()
}

internal fun Project.buildDependenciesString(): String {
    val plugin = configurations.getByName("plugin")
    val optionalPlugin = configurations.getByName("optionalPlugin")

    val required = plugin.allDependencies.map(Dependency::toDependencyString)
    val optional = optionalPlugin.allDependencies.map { it.toDependencyString(true) }

    return (required + optional).joinToString(", ")
}

internal fun Dependency.toDependencyString(optional: Boolean = false): String {
    val name = if (this is ProjectDependency) {
        dependencyProject.pluginId
    } else {
        name.substringAfter("mikbot-")
    }

    return "$name${if (optional) "?" else ""}@>=$version"
}

/**
 * Tries to configure this project for raw PF4J.
 */
@Suppress("unused")
fun Project.usePF4J() {
    extensions.configure<PluginExtension>(pluginExtensionName) {
        ignoreDependencies.set(true)
        pluginMainFileLocation.set(
            buildDir.resolve("resources").resolve("main").resolve("plugin.properties").toPath()
        )
    }
}

fun Path.sha512Checksum(): String = readBytes().sha512Checksum()

fun ByteArray.sha512Checksum(): String {
    val digest = MessageDigest.getInstance("SHA-512")
    val hashBytes = digest.digest(this)
    return hashBytes.fold("") { str, it -> str + "%02x".format(it) }
}

internal fun pluginNotAppliedError(name: String): Nothing =
    error("Please make sure the $name plugin is applied before the mikbot plugin")

