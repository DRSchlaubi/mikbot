package dev.schlaubi.mikbot.plugins.gradle.publishing

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

abstract class MakeRepositoryIndexTask : DefaultTask() {

    @get:InputDirectory
    abstract val targetDirectory: Property<Path>

    @get:Input
    abstract val repositoryUrl: Property<String>

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
                "https://github.com/DRSchlaubi/mikbot${project.path.replace(":", "/")}",
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
