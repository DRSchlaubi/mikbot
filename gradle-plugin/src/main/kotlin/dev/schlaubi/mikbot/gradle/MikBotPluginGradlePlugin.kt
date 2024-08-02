package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.createExtensions
import dev.schlaubi.mikbot.gradle.extension.mikbotPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*

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
                configureSourceSets()
                target.afterEvaluate {
                    (target.extensions.getByName("sourceSets") as SourceSetContainer).getByName("main")
                        .apply {
                            resources.srcDir(
                                target.layout.buildDirectory.dir("generated/mikbot/main/resources")
                            )
                        }
                }
            }
        }
    }

    private fun Project.configureTasks() {
        val generateDefaultTranslationBundle by tasks.registering(GenerateDefaultTranslationBundleTask::class) {
            defaultLocale = mikbotPluginExtension.defaultLocale
        }
        val (assemblePlugin, installBotTask) = tasks.createAssembleTasks(generateDefaultTranslationBundle)
        createTestBotTasks(assemblePlugin, installBotTask)
        createPublishingTasks(assemblePlugin)
    }

    private fun Project.configureSourceSets() {
        configure<SourceSetContainer> {
            named("main") {
                java {
                    val optionalKspSourceSet = mikbotPluginExtension.enableKordexProcessor.map {
                        if (it) {
                            layout.buildDirectory.dir("/generated/ksp/main/kotlin/")
                        } else {
                            fileTree("never_exists") {
                                include { false } // include none
                            }
                        }
                    }
                    srcDir(optionalKspSourceSet)
                }
            }
        }
    }

    private fun Project.createTestBotTasks(assemblePlugin: TaskProvider<Zip>, installBotTask: Provider<InstallBotTask>) {
        tasks.run {
            val installPlugins = task<InstallPluginsToTestBotTask>("installPluginsToTestBot") {
                dependsOn(assemblePlugin)

                pluginArchive = assemblePlugin
            }

            task<RunBotTask>("runBot") {
                dependsOn(installBotTask, installPlugins)
                installTask = installBotTask
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
