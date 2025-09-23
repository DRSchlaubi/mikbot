package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.PluginExtension
import dev.schlaubi.mikbot.gradle.extension.pluginExtensionName
import dev.schlaubi.mikbot.gradle.extension.pluginId
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.readBytes
import kotlin.io.path.readText

internal fun readPluginsJson(path: Path): List<PluginInfo> = path.readText().run { Json.decodeFromString(this) }

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

internal val PluginExtension.pluginFilePath: String
    get() = "${pluginId.get()}/${version.get()}/plugin-${pluginId.get()}-${version.get()}.zip"

private data class PluginWrapper(val pluginInfo: PluginInfo) {
    override fun equals(other: Any?): Boolean = (other as? PluginWrapper)?.pluginInfo?.id == pluginInfo.id
    override fun hashCode(): Int = pluginInfo.id.hashCode()
}

internal fun Project.buildDependenciesString(): String {
    val plugin = configurations.getByName("plugin")
    val optionalPlugin = configurations.getByName("optionalPlugin")

    val required = plugin.allDependencies.map { it.toDependencyString(rootProject) }
    val optional = optionalPlugin.allDependencies.map { it.toDependencyString(rootProject, true) }

    return (required + optional).joinToString(", ")
}

internal fun Dependency.toDependencyString(rootProject: Project, optional: Boolean = false): String {
    val safeVersion = version.toString().substringBefore("-SNAPSHOT")
    @Suppress("DEPRECATION") // Currently no better way available
    val name = if (this is ProjectDependency) {
        val dependencyProject = rootProject.project(path)
        dependencyProject.pluginId
    } else {
        name.substringAfter("mikbot-")
    }

    return "$name${if (optional) "?" else ""}@>=$safeVersion"
}

/**
 * Tries to configure this project for raw PF4J.
 */
@Suppress("unused")
fun Project.usePF4J() {
    extensions.configure<PluginExtension>(pluginExtensionName) {
        ignoreDependencies = true
        pluginMainFileLocation = layout.buildDirectory.file("resources/main/plugin.properties")
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

internal fun Provider<Directory>.dir(name: String) = map { it.dir(name) }
internal fun Provider<Directory>.file(name: String) = map { it.file(name) }
internal fun Property<out FileSystemLocation>.asPath() = get().asFile.toPath()
internal fun FileSystemLocation.asPath() = asFile.toPath()
