package dev.schlaubi.mikbot.gradle.extension

import dev.schlaubi.mikbot.gradle.BuildRepositoryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

const val pluginExtensionName = "mikbotPlugin"

@Suppress("ConstPropertyName")
private const val pluginPublishingExtensionName = "pluginPublishing"

// This is just there for usage in the Plugin, users of the plugin should use Gradle's generated
// accessors
internal val Project.mikbotPluginExtension: PluginExtension
    get() = findExtension(pluginExtensionName) ?: error("Missing MikBot plugin in :path")

// This is just there for usage in the Plugin, users of the plugin should use Gradle's generated accessors
internal val Project.pluginPublishingExtension: BuildRepositoryExtension
    get() = findExtension(pluginPublishingExtensionName) ?: error("Missing MikBot plugin in :path")


fun Project.createExtensions() {
    extensions.create<PluginExtension>(pluginExtensionName).apply {
        if (parent != null) {
            val base = rootProject.findExtension<PluginExtension>(pluginExtensionName) ?: return@apply
            pluginId.convention(base.pluginId.orNull)
            requires.convention(base.requires.orNull)
            description.convention(base.description.orNull)
            provider.convention(base.provider.orNull)
            license.convention(base.license.orNull)
            ignoreDependencies.convention(base.ignoreDependencies.orNull)
            pluginMainFileLocation.convention(base.pluginMainFileLocation.orNull)
            bundle.convention(base.bundle.orNull)
        }
    }
    extensions.create<BuildRepositoryExtension>(pluginPublishingExtensionName).apply {
        if (parent != null) {
            val base =
                rootProject.findExtension<BuildRepositoryExtension>(pluginPublishingExtensionName) ?: return@apply
            targetDirectory.convention(base.targetDirectory)
            currentRepository.convention(base.currentRepository)
            repositoryUrl.convention(base.repositoryUrl)
            projectUrl.convention(base.projectUrl)
        }
    }
}

private inline fun <reified T> ExtensionContainer.create(name: String) = create(name, T::class.java)

internal inline fun <reified T> ExtensionAware.findExtension(name: String) =
    extensions.findByName(name) as T?
