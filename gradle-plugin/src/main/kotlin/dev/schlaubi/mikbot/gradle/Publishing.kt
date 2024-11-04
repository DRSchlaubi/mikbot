package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.mikbotPluginExtension
import dev.schlaubi.mikbot.gradle.extension.pluginId
import dev.schlaubi.mikbot.gradle.extension.pluginPublishingExtension
import org.gradle.api.Project
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register
import java.nio.file.Files

internal fun Project.createPublishingTasks(assemblePluginTask: TaskProvider<Zip>) {
    tasks.apply {
        val pluginFile = assemblePluginTask.get().archiveFileName.get()
        val pluginExtension = project.mikbotPluginExtension
        val pluginPublishingExtension = project.pluginPublishingExtension

        val copyFilesIntoRepo = register<Copy>("copyFilesIntoRepo") {
            group = PublishingPlugin.PUBLISH_TASK_GROUP

            from(assemblePluginTask)
            include("*.zip")
            // providing version manually, as of weird evaluation errors
            into(pluginPublishingExtension.targetDirectory.get().asPath().resolve("${pluginExtension.pluginId.get()}/$version"))

            eachFile {
                val parent = pluginPublishingExtension.currentRepository.get().asPath()

                val destinationPath = destinationDir.toPath()
                val probableExistingFile =
                    parent.resolve(
                        destinationPath.subpath(
                            destinationPath.nameCount - 2,
                            destinationPath.nameCount
                        )
                    ).resolve(pluginFile)

                if (Files.exists(probableExistingFile)) {
                    exclude() // exclude existing files, so checksums don't change
                }
            }
        }

        val repo = register("buildRepository") {
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            dependsOn(copyFilesIntoRepo)
        }

        afterEvaluate {
            val makeIndex = register<MakeRepositoryIndexTask>("makeRepositoryIndex") {
                group = PublishingPlugin.PUBLISH_TASK_GROUP
                dependsOn(assemblePluginTask)
            }

            repo.configure {
                dependsOn(makeIndex)
            }
        }
    }
}
