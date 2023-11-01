package dev.schlaubi.mikbot.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory

abstract class BuildRepositoryExtension {
    /**
     * The directory to save the repository to.
     */
    @get:InputDirectory
    abstract val targetDirectory: DirectoryProperty

    /**
     * Directory representing the current repository content (defaults to [targetDirectory]).
     */
    @get:InputDirectory
    abstract val currentRepository: DirectoryProperty

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


