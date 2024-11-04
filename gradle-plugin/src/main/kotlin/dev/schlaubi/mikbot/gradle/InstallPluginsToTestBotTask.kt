package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.pluginId
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import javax.inject.Inject

abstract class InstallPluginsToTestBotTask : DefaultTask() {

    @get:Input
    abstract val pluginArchive: Property<RegularFile>

    @get:OutputDirectory
    val outputDirectory: Provider<Directory> = project.layout.buildDirectory.dir("test-bot/plugins")

    @get:Inject
    abstract val fs: FileSystemOperations

    @TaskAction
    fun install() {
        val result = fs.copy {
            from(pluginArchive.get())
            into(outputDirectory)
            rename { "plugin-${project.pluginId}.zip" }
        }

        didWork = result.didWork
    }
}
