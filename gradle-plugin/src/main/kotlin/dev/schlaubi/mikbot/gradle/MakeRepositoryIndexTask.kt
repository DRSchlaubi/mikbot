package dev.schlaubi.mikbot.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import java.util.*

abstract class MakeRepositoryIndexTask : DefaultTask() {

    init {
        outputs.dir(project.pluginPublishingExtension.targetDirectory)
    }

    @TaskAction
    fun upload() {
        val publishingExtension = project.pluginPublishingExtension
        val pluginsPath = publishingExtension.targetDirectory.get().resolve("plugins.json")
        val plugins = if (Files.exists(pluginsPath)) {
            readPluginsJson(pluginsPath)
        } else {
            emptyList()
        }

        val pluginTask = project.tasks.getByName("assemblePlugin") as Jar

        val extension = project.mikbotPluginExtension
        val newPlugins = plugins.addPlugins(
            PluginInfo(
                project.pluginId,
                project.name,
                extension.description.getOrElse(""),
                publishingExtension.projectUrl.get(),
                listOf(
                    PluginRelease(
                        project.version as String,
                        Date(),
                        extension.requires.getOrElse(project.project(":").version as String),
                        publishingExtension.repositoryUrl.get() + "/" + project.pluginFilePath,
                        pluginTask.archiveFile.get().asFile.toPath().sha512Checksum()
                    )
                )
            )
        )

        newPlugins.writeTo(pluginsPath)
    }
}
