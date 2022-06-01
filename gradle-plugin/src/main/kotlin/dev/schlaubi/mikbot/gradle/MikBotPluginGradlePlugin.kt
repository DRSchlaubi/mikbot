package dev.schlaubi.mikbot.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

// There might be a better way of doing this, but for now I can't be bothered figuring it out
private val Project.pluginMainFile: Path
    get() = buildDir
        .resolve("generated")
        .resolve("ksp")
        .resolve("main")
        .resolve("resources")
        .resolve("META-INF")
        .resolve("plugin.properties")
        .toPath()

@Suppress("unused")
class MikBotPluginGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            createPluginExtensions()
            configureTasks()
            addRepositories()
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

    private fun Project.configureTasks() {
        val patchPropertiesTask = createPatchPropertiesTask()

        val jar = tasks.findByName("jar") as Jar? ?: pluginNotAppliedError("Kotlin")
        jar.dependsOn(patchPropertiesTask)
        val assemblePlugin = createAssemblePluginTask(jar)
        val installBotTask = tasks.create("installBot", InstallBotTask::class.java)
        createPublishingTasks(assemblePlugin)
        createTestBotTasks(assemblePlugin, installBotTask)
        createAssembleBotTask(assemblePlugin, installBotTask)
    }

    private fun Project.createTestBotTasks(assemblePlugin: TaskProvider<Jar>, installBotTask: InstallBotTask) {
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

    private fun Project.createPatchPropertiesTask() =
        tasks.run {
            task<PatchPropertiesTask>("patchPluginProperties") {
                group = "mikbot-plugin"
                dependsOn("kspKotlin")
                propertiesDirectory.set(
                    project
                        .mikbotPluginExtension
                        .pluginMainFileLocation
                        .getOrElse(project.pluginMainFile)
                        .parent
                )

                doFirst {
                    if (!project.tasks.getByName("kspKotlin").didWork) {
                        this.didWork = false
                    }
                }
            }
        }

    private fun Project.createAssembleBotTask(assemblePlugin: TaskProvider<Jar>, installBotTask: InstallBotTask) {
        tasks.run {
            val assembleBot = register<Jar>("assembleBot") {
                dependsOn(assemblePlugin, installBotTask)

                group = "mikbot"

                destinationDirectory.set(buildDir.resolve("bot"))
                archiveBaseName.set("bot-${project.name}")
                archiveExtension.set("zip")

                into("") {
                    // make this lazy, so it doesn't throw at initialization
                    val provider = provider {
                        val version = installBotTask.botVersionFromProject()
                        installBotTask.testBotFolder.resolve("mikmusic-$version")
                    }
                    it.from(provider)
                }
                into("lib/bundled-plugins") {
                    val task = assemblePlugin.get()
                    it.from(task.archiveFile)
                }
            }

            register<Copy>("installBotArchive") {
                dependsOn(assembleBot)

                from(zipTree(assembleBot.get().archiveFile))
                into(buildDir.resolve("installBot"))
            }
        }
    }


    // Taken from: https://github.com/twatzl/pf4j-kotlin-demo/blob/master/plugins/build.gradle.kts#L20-L35
    // Funfact: because the kotlin dsl is missing we only have groovy api
    // this means all the lambdas are normal lambdas and not lambdas with receivers
    // therefore not calling it, would always use the main spec
    // which took me 4 hrs to figure out
    private fun Project.createAssemblePluginTask(jarTask: Jar) =
        tasks.run {
            register<Jar>("assemblePlugin") {
                group = "build"
                dependsOn(jarTask)

                duplicatesStrategy = DuplicatesStrategy.EXCLUDE

                destinationDirectory.set(buildDir.resolve("plugin"))
                archiveBaseName.set("plugin-${project.pluginId}")
                archiveExtension.set("zip")

                // first taking the classes generated by the jar task
                into("classes") {
                    it.with(jarTask)
                }

                // and then we also need to include any libraries that are needed by the plugin
                dependsOn(configurations.getByName("runtimeClasspath"))
                into("lib") {
                    it.from({
                        val mainConfiguration = if (!project.mikbotPluginExtension
                                .ignoreDependencies
                                .getOrElse(false)
                        ) {
                            transientDependencies
                                .lines()
                                .filterNot { file -> file.startsWith("#") || file.isBlank() }
                        } else {
                            emptyList()
                        }

                        // filter out dupe dependencies
                        configurations.getByName("runtimeClasspath").resolvedConfiguration.resolvedArtifacts.asSequence()
                            .filter { dep ->
                                (dep.moduleVersion.id.group + ":" + dep.moduleVersion.id.name) !in mainConfiguration
                            }.mapNotNull { dep ->
                                dep.file
                            }.toList()
                    })
                }

                into("") { // not specifying "" brakes Gradle btw
                    val file = project.mikbotPluginExtension.pluginMainFileLocation
                        .getOrElse(pluginMainFile)
                    it.from(file.parent)
                    it.include(file.name)
                }
            }
        }

    private fun Project.createPluginExtensions() {
        extensions.create<PluginExtension>(pluginExtensionName)

        val mikbotConfiguration = configurations.create("mikbot")

        val pluginConfiguration = configurations.create("plugin")
        val optionalPluginConfiguration = configurations.create("optionalPlugin")

        val compileOnly = configurations.findByName("compileOnly")
            ?: pluginNotAppliedError("Kotlin")

        compileOnly.apply {
            extendsFrom(pluginConfiguration)
            extendsFrom(optionalPluginConfiguration)
            extendsFrom(mikbotConfiguration)
        }
    }

    private fun Project.createPublishingTasks(assemblePluginTask: TaskProvider<Jar>) {
        val extension = project.extensions.create(pluginPublishingExtensionName, BuildRepositoryExtension::class.java)
        tasks.apply {
            val copyFilesIntoRepo = register<Copy>("copyFilesIntoRepo") {
                group = "publishing"

                from(assemblePluginTask)
                include("*.zip")
                // providing version manually, as of weird evaluation errors
                into(extension.targetDirectory.get().resolve("${project.pluginId}/$version"))

                eachFile {
                    val parent = extension.currentRepository.getOrElse(extension.targetDirectory.get())
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
                        it.exclude() // exclude existing files, so checksums don't change
                    }
                }
            }

            val repo = register("buildRepository") {
                group = "publishing"
                it.dependsOn(copyFilesIntoRepo)
            }

            afterEvaluate {
                val makeIndex = register<MakeRepositoryIndexTask>("makeRepositoryIndex") {
                    group = "publishing"
                    dependsOn(assemblePluginTask)
                }

                repo.configure {
                    it.dependsOn(makeIndex)
                }
            }
        }
    }
}

private inline fun <reified T> ExtensionContainer.create(name: String) = create(name, T::class.java)
private inline fun <reified T : Task> TaskContainer.task(name: String, crossinline block: T.() -> Unit) =
    create(name, T::class.java) {
        it.block()
    }

private inline fun <reified T : Task> TaskContainer.register(name: String, crossinline block: T.() -> Unit) =
    register(name, T::class.java) {
        it.block()
    }

private fun pluginNotAppliedError(name: String): Nothing =
    error("Please make sure the $name plugin is applied before the mikbot plugin")
