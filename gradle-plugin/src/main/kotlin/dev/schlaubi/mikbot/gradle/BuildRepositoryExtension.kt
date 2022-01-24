package dev.schlaubi.mikbot.gradle

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import java.nio.file.Path

abstract class BuildRepositoryExtension {
    /**
     * The directory to save the repository to.
     */
    @get:InputDirectory
    abstract val targetDirectory: Property<Path>

    /**
     * The URL were the repository is hosted (used for URLs in plugins.json).
     */
    @get:Input
    abstract val repositoryUrl: Property<String>

    /**
     * The URL of this project.
     */
    @get:Input
    abstract val projectUrl: Property<String>
}

internal const val pluginPublishingExtensionName = "pluginPublishing"

// This is just there for usage in the Plugin, users of the plugin should use Gradle's generated accessors
internal val ExtensionAware.pluginPublishingExtension: BuildRepositoryExtension
    get() = extensions.findByName(pluginPublishingExtensionName) as BuildRepositoryExtension