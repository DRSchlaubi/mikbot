package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.pluginId
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class InstallPluginsToTestBotTask : DefaultTask() {

    @get:InputFile
    abstract val pluginArchive: RegularFileProperty

    @get:OutputDirectory
    val outputDirectory: Provider<Directory> = project.layout.buildDirectory.dir("test-bot/plugins")

    @get:Inject
    abstract val fs: FileSystemOperations

    @TaskAction
    fun install() {
        val result = fs.copy {
            from(pluginArchive)
            into(outputDirectory)
            rename { "plugin-${project.pluginId}.zip" }
        }

        didWork = result.didWork
    }
}
