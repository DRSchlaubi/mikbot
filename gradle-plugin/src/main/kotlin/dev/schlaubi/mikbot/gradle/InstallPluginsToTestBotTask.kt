package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.pluginId
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip

abstract class InstallPluginsToTestBotTask : DefaultTask() {

    @get:Input
    abstract val pluginArchive: Property<TaskProvider<Zip>>

    @TaskAction
    fun install() {
        val task = pluginArchive.get().get()

        val result = project.copy {
            from(task.destinationDirectory)
            include(task.archiveFile.get().asFile.name)
            into(project.layout.buildDirectory.dir("test-bot/plugins"))
            rename { "plugin-${project.pluginId}.zip" }
        }

        didWork = result.didWork
    }
}
