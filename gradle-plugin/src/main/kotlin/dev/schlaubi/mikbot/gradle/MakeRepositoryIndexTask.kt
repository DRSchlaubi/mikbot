package dev.schlaubi.mikbot.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

abstract class MakeRepositoryIndexTask : DefaultTask() {

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

    @TaskAction
    fun upload() {
        val pluginsPath = targetDirectory.get().resolve("plugins.json")
        val plugins = if (Files.exists(pluginsPath)) {
            readPluginsJson(pluginsPath)
        } else {
            emptyList()
        }

        val extension = project.mikbotPluginExtension
        val newPlugins = plugins.addPlugins(
            PluginInfo(
                project.name,
                project.name,
                extension.description.getOrElse(""),
                projectUrl.get(),
                listOf(
                    PluginRelease(
                        project.version as String,
                        Date(),
                        extension.requires.getOrElse(project.project(":").version as String),
                        repositoryUrl.get() + "/" + project.pluginFilePath
                    )
                )
            )
        )

        newPlugins.writeTo(pluginsPath)
    }
}
