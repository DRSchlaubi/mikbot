package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.mikbotPluginExtension
import dev.schlaubi.mikbot.gradle.extension.pluginId
import dev.schlaubi.mikbot.gradle.extension.pluginPublishingExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.getByName
import java.nio.file.Files
import java.util.*
import kotlin.io.path.exists

abstract class MakeRepositoryIndexTask : DefaultTask() {

    init {
        outputs.dir(project.pluginPublishingExtension.targetDirectory)
    }

    private val publishingExtension = project.pluginPublishingExtension
    private val pluginArchive = project.tasks.getByName<Zip>("assemblePlugin").archiveFile
    private val extension = project.mikbotPluginExtension
    private val projectName = project.name

    @TaskAction
    fun upload() {
        val pluginsPath = publishingExtension.targetDirectory.asPath().resolve("plugins.json")
        val plugins = if (pluginsPath.exists()) {
            readPluginsJson(pluginsPath)
        } else {
            emptyList()
        }

        val newPlugins = plugins.addPlugins(
            PluginInfo(
                extension.pluginId.get(),
                projectName,
                extension.description.getOrElse(""),
                publishingExtension.projectUrl.get(),
                listOf(
                    PluginRelease(
                        extension.version.get().substringBefore("-SNAPSHOT"),
                        Date(),
                        extension.requires.get(),
                        publishingExtension.repositoryUrl.get() + "/" + extension.pluginFilePath,
                        pluginArchive.get().asFile.toPath().sha512Checksum()
                    )
                )
            )
        )

        newPlugins.writeTo(pluginsPath)
    }
}
