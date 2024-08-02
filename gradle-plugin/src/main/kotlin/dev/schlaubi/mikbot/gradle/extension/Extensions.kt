package dev.schlaubi.mikbot.gradle.extension

import dev.schlaubi.mikbot.gradle.BuildRepositoryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import java.util.*

const val pluginExtensionName = "mikbotPlugin"

private const val pluginPublishingExtensionName = "pluginPublishing"

// This is just there for usage in the Plugin, users of the plugin should use Gradle's generated
// accessors
internal val Project.mikbotPluginExtension: PluginExtension
    get() = extensions.findByType() ?: error("Missing MikBot plugin in :path")

// This is just there for usage in the Plugin, users of the plugin should use Gradle's generated accessors
internal val Project.pluginPublishingExtension: BuildRepositoryExtension
    get() = extensions.findByType() ?: error("Missing MikBot plugin in :path")


fun Project.createExtensions() {
    extensions.create<PluginExtension>(pluginExtensionName).apply {
        if (parent != null) {
            val base = rootProject.extensions.findByType<PluginExtension>() ?: return@apply
            pluginId.convention(base.pluginId)
            requires.convention(base.requires)
            description.convention(base.description)
            provider.convention(base.provider)
            license.convention(base.license)
            ignoreDependencies.convention(base.ignoreDependencies)
            pluginMainFileLocation.convention(base.pluginMainFileLocation)
            bundle.convention(base.bundle)
            enableKordexProcessor
                .convention(base.enableKordexProcessor.convention(false))
                .convention(false)
            defaultLocale.convention(base.defaultLocale.convention(Locale.ENGLISH))
            repositories.convention(emptyList())
        }
    }
    extensions.create<BuildRepositoryExtension>(pluginPublishingExtensionName).apply {
        if (parent != null) {
            val base =
                rootProject.extensions.findByType<BuildRepositoryExtension>() ?: return@apply
            targetDirectory.convention(base.targetDirectory)
            currentRepository.convention(base.currentRepository)
            repositoryUrl.convention(base.repositoryUrl)
            projectUrl.convention(base.projectUrl)
        }
    }
}

