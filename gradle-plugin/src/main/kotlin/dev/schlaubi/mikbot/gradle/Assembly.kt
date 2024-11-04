package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.mikbotPluginExtension
import dev.schlaubi.mikbot.gradle.extension.pluginId
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

// There might be a better way of doing this, but for now I can't be bothered figuring it out
private val Project.pluginMainFile: Provider<RegularFile>
    get() = layout.buildDirectory
        .file("generated/ksp/main/resources/META-INF/plugin.properties")

internal data class AssemblyTask(val assembleTask: TaskProvider<Zip>, val installBotTask: TaskProvider<InstallBotTask>)


private fun Project.createInstallBotTask(): TaskProvider<InstallBotTask> =
    tasks.register<InstallBotTask>("installBot")

context(Project)
internal fun TaskContainer.createAssembleTasks(): AssemblyTask {
    val patchPropertiesTask = createPatchPropertiesTask()

    val jar = tasks.findByName("jar") as Jar? ?: pluginNotAppliedError("Kotlin")
    jar.dependsOn(patchPropertiesTask)
    val assembleTask = createAssemblePluginTask(jar)
    val installBotTask = createInstallBotTask()

    // This task only makes sense for plugin projects, since mikbot core plugins aren't distributed as standalone bots
    // Also this is defined though the Gradle environment, therefore it is a compile-time constant
    // But compilation of public and internal variant is different
    @Suppress("KotlinConstantConditions")
    if (!MikBotPluginInfo.IS_MIKBOT) {
        createAssembleBotTask(assembleTask, installBotTask)
    }
    return AssemblyTask(assembleTask, installBotTask)
}

context(Project)
private fun TaskContainer.createAssembleBotTask(
    assemblePlugin: TaskProvider<Zip>,
    installBotTask: TaskProvider<InstallBotTask>,
) {
    apply<DistributionPlugin>()

    extensions.configure<DistributionContainer> {
        create("bot") {
            distributionBaseName = "bot-${project.name}"

            contents {
                into("/") {
                    from(installBotTask) {
                        eachFile {
                            val rootDir = "bot-${MikBotPluginInfo.VERSION}"
                            path = path.removePrefix(rootDir)
                        }
                    }

                    val installedPluginsName = "lib/bundled-plugins"
                    into(installedPluginsName) {
                        from(assemblePlugin.flatMap(Zip::getArchiveFile))
                    }
                    into(installedPluginsName) {
                        from({
                            val dependencies =
                                (configurations.getByName("plugin").allDependencies +
                                    configurations.getByName("optionalPlugin").allDependencies)
                                    .filterIsInstance<ModuleDependency>()
                                    .map {
                                        dependencies.create(
                                            mapOf(
                                                "name" to it.name.replace("mikbot-", ""),
                                                "version" to it.version,
                                                "ext" to "zip"
                                            )
                                        )
                                    }

                            configurations.detachedConfiguration(*dependencies.toTypedArray()).resolve()
                        })
                        include("*.zip")
                    }
                }
            }
        }
    }
}

context(Project)
private fun TaskContainer.createPatchPropertiesTask() =
    register<PatchPropertiesTask>("patchPluginProperties") {
        group = "mikbot-plugin"
        dependsOn("kspKotlin")
        propertiesDirectory = (project
            .mikbotPluginExtension
            .pluginMainFileLocation
            .orNull ?: project.pluginMainFile.get())
            .asFile
            .parentFile
    }

// Taken from: https://github.com/twatzl/pf4j-kotlin-demo/blob/master/plugins/build.gradle.kts#L20-L35
// Funfact: because the kotlin dsl is missing we only have groovy api
// this means all the lambdas are normal lambdas and not lambdas with receivers
// therefore not calling it, would always use the main spec
// which took me 4 hrs to figure out
// Funfact 2: 2 yrs later I figured out I am supposed to apply the "kotlin-dsl" plugin
context(Project)
private fun TaskContainer.createAssemblePluginTask(jarTask: Jar) =
    register<Zip>("assemblePlugin") {
        val extension = project.mikbotPluginExtension
        group = LifecycleBasePlugin.BUILD_GROUP
        dependsOn(jarTask)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        destinationDirectory = layout.buildDirectory.dir("plugin")
        archiveBaseName = extension.pluginId.map { "plugin-$it" }
        archiveExtension = "zip"

        // first taking the classes generated by the jar task
        into("classes") {
            with(jarTask)
        }

        // and then we also need to include any libraries that are needed by the plugin
        dependsOn(configurations.getByName("runtimeClasspath"))
        into("lib") {
            from({
                val mainConfiguration = if (!extension.ignoreDependencies.getOrElse(false)) {
                    transientDependencies
                        .lines()
                        .filterNot { file -> file.startsWith("#") || file.isBlank() }
                } else {
                    emptyList()
                }

                // filter out dupe dependencies
                configurations.getByName("runtimeClasspath").resolvedConfiguration.resolvedArtifacts.asSequence()
                    .filter { dep ->
                        val idWithoutPlatform = dep.moduleVersion.id.name.substringBefore("-jvm")
                        (dep.moduleVersion.id.group + ":" + idWithoutPlatform) !in mainConfiguration
                    }.mapNotNull { dep ->
                        dep.file
                    }.toList()
            })
        }

        into("") { // not specifying "" brakes Gradle btw
            val file = (extension.pluginMainFileLocation.orNull ?: pluginMainFile.get()).asFile
            from(file.parent)
            include(file.name)
        }
    }

