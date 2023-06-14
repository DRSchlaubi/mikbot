package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.createExtensions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.task

@Suppress("unused")
class MikBotPluginGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            createExtensions()
            // Do not create assemble tasks for root project (multimodule support)
            if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
                createConfigurations()
                configureTasks()
                addRepositories()
                addDependencies()
                target.afterEvaluate {
                    (target.extensions.getByName("sourceSets") as SourceSetContainer).getByName("main")
                        .apply {
                            resources.srcDir(
                                target.buildDir.resolve("generated").resolve("mikbot").resolve("main")
                                    .resolve("resources")
                            )
                        }
                }
            }
        }
    }

    private fun Project.configureTasks() {
        val (assemblePlugin, installBotTask) = tasks.createAssembleTasks()
        createTestBotTasks(assemblePlugin, installBotTask)
        createPublishingTasks(assemblePlugin)
    }

    private fun Project.createTestBotTasks(assemblePlugin: TaskProvider<Zip>, installBotTask: InstallBotTask) {
        tasks.run {
            val installPlugins = task<InstallPluginsToTestBotTask>("installPluginsToTestBot") {
                dependsOn(assemblePlugin)

                pluginArchive.set(assemblePlugin)
            }

            task<RunBotTask>("runBot") {
                dependsOn(installBotTask, installPlugins)
                installTask.set(installBotTask)
            }
        }
    }

    private fun Project.createConfigurations() {
        val pluginConfiguration = configurations.create("plugin")
        val optionalPluginConfiguration = configurations.create("optionalPlugin")

        val compileOnly = configurations.findByName("compileOnly")
            ?: pluginNotAppliedError("Kotlin")
        compileOnly.apply {
            extendsFrom(pluginConfiguration)
            extendsFrom(optionalPluginConfiguration)
        }
    }
}
