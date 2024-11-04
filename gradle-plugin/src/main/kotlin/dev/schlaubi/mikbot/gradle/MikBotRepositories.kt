package dev.schlaubi.mikbot.gradle

import dev.schlaubi.mikbot.gradle.extension.mikbotPluginExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.repositories
import java.net.URI

internal fun Project.addRepositories() {
    repositories {
        mavenCentral()
        maven {
            name = "Mikbot"
            url = uri("https://europe-west3-maven.pkg.dev/mik-music/mikbot")
        }

        maven {
            name = "KordEx (Releases)"
            url = uri("https://repo.kordex.dev/releases")
        }

        maven {
            name = "KordEx (Snapshots)"
            url = uri("https://repo.kordex.dev/snapshots")
        }

        maven {
            name = "Sonatype Snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            name = "Sonatype Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }

        mikbotBinaries()
        mikbotPlugins("https://storage.googleapis.com/mikbot-plugins")
    }
}

internal fun Project.addDependencies() {
    dependencies.apply {
        // this one is included in the bot itself
        add("compileOnly", "org.jetbrains.kotlin:kotlin-stdlib")
        add("compileOnly", mikbot("api"))
        if (configurations.findByName("ksp") != null) {
            add("ksp", mikbot("plugin-processor"))
            val optionalKordExDependency = mikbotPluginExtension.enableKordexProcessor.map {
                if (it) {
                    create("com.kotlindiscord.kord.extensions:annotation-processor:${MikBotPluginInfo.KORDEX_VERSION}")
                } else {
                    val emptyDependency = project.fileTree("empty") {
                        include { false }
                    }
                    create(emptyDependency)
                }
            }
            add("ksp", optionalKordExDependency)
        } else {
            logger.warn("Could not add KSP processor automatically, because KSP plugin is not installed!")
        }
    }
}

/**
 * Creates a [Dependency] with [group] and [name] with the current mikbot version.
 */
fun DependencyHandlerScope.mikbot(group: String, name: String): Dependency =
    create("${group}:${name}:${MikBotPluginInfo.snapshotVersion}")

/**
 * Specifies the correct mikbot version on this [MinimalExternalModuleDependency].
 */
fun DependencyHandlerScope.mikbot(dependency: MinimalExternalModuleDependency): Dependency {
    val module = dependency.module
    return mikbot(module.group, module.name)
}

/**
 * Specifies the correct mikbot version on this [MinimalExternalModuleDependency].
 */
@Suppress("ConvertLambdaToReference")
fun DependencyHandlerScope.mikbot(dependency: Provider<MinimalExternalModuleDependency>) =
    dependency.map { mikbot(it) }

/**
 * Specifies the correct mikbot version on this [MinimalExternalModuleDependency].
 */
fun DependencyHandlerScope.mikbot(dependency: ProviderConvertible<MinimalExternalModuleDependency>) =
    mikbot(dependency.asProvider())

private fun DependencyHandler.mikbot(module: String): Any =
    if (MikBotPluginInfo.IS_MIKBOT) project(mapOf("path" to ":$module")) else "dev.schlaubi:mikbot-$module:${MikBotPluginInfo.snapshotVersion}"

private val MikBotPluginInfo.snapshotVersion
    get() = "$VERSION-SNAPSHOT"

private fun RepositoryHandler.gcs(url: String, pattern: String) = ivy {
    this.url = URI(url)
    patternLayout {
        artifact(pattern)
    }
    metadataSources {
        artifact()
    }
}

private fun RepositoryHandler.mikbotBinaries() =
    gcs("https://storage.googleapis.com/mikbot-binaries", "[revision]/[artifact]-[revision].[ext]")

fun RepositoryHandler.mikbotPlugins(url: String) = gcs(url, "[artifact]/[revision]/plugin-[artifact]-[revision].[ext]")
