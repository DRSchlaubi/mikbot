package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.pluginId
import dev.schlaubi.mikbot.gradle.extension.pluginPublishingExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register
import java.nio.file.Files

internal fun Project.createPublishingTasks(assemblePluginTask: TaskProvider<Zip>) {
    tasks.apply {
        val copyFilesIntoRepo = register<Copy>("copyFilesIntoRepo") {
            group = "publishing"

            from(assemblePluginTask)
            include("*.zip")
            // providing version manually, as of weird evaluation errors
            into(
                pluginPublishingExtension.targetDirectory.asPathOrElse(project.file("ci-repo").toPath())
                    .resolve("${project.pluginId}/$version")
            )

            eachFile {
                val parent = pluginPublishingExtension.currentRepository.getOrElse(pluginPublishingExtension.targetDirectory.get())
                    .asPath()

                val destinationPath = destinationDir.toPath()
                val probableExistingFile =
                    parent.resolve(
                        destinationPath.subpath(
                            destinationPath.nameCount - 2,
                            destinationPath.nameCount
                        )
                    )
                        .resolve(assemblePluginTask.get().archiveFileName.get())

                if (Files.exists(probableExistingFile)) {
                    exclude() // exclude existing files, so checksums don't change
                }
            }
        }

        val repo = register("buildRepository") {
            group = "publishing"
            dependsOn(copyFilesIntoRepo)
        }

        afterEvaluate {
            val makeIndex = register<MakeRepositoryIndexTask>(/* name = */ "makeRepositoryIndex") {
                group = "publishing"
                dependsOn(assemblePluginTask)
            }

            repo.configure {
                dependsOn(makeIndex)
            }
        }
    }
}
