package dev.schlaubi.mikbot.gradle.extension

import dev.schlaubi.mikbot.gradle.BuildRepositoryExtension
import dev.schlaubi.mikbot.gradle.MikBotPluginInfo
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
            requires.convention(base.requires)
            description.convention(base.description)
            provider.convention(base.provider)
            license.convention(base.license)
            ignoreDependencies.convention(base.ignoreDependencies)
            pluginMainFileLocation.convention(base.pluginMainFileLocation)
            enableKordexProcessor
                .convention(base.enableKordexProcessor.convention(false))
                .convention(false)
            repositories.convention(emptyList())
            i18n {
                classPackage.convention(base.i18n.classPackage)
                configureSourceSet.convention(base.i18n.configureSourceSet)
                outputDirectory.convention(base.i18n.outputDirectory)
                publicVisibility.convention(base.i18n.publicVisibility)
            }
        }
        pluginId.convention(name)
        bundle.convention(pluginId)

        i18n {
            translationBundle.convention(bundle)
            className.convention(translationBundle.map {
                it.split("[-_]".toRegex()).joinToString("") { section ->
                    section.replaceFirstChar(Char::uppercase)
                } + "Translations"
            })
            requires.convention(MikBotPluginInfo.VERSION)
            version.convention(project.version.toString())
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
        } else {
            targetDirectory.convention(layout.projectDirectory.dir("ci-repo"))
            currentRepository.convention(pluginPublishingExtension.targetDirectory)
        }
    }
}

