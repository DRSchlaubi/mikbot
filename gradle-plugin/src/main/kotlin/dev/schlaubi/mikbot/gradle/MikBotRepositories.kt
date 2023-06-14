package dev.schlaubi.mikbot.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.DependencyHandlerScope

internal fun Project.addRepositories() {
    repositories.mavenCentral()
    repositories.maven {
        name = "Mikbot"
        url = uri("https://europe-west3-maven.pkg.dev/mik-music/mikbot")
    }
    repositories.maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
    repositories.maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    repositories.maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

internal fun Project.addDependencies() {
    dependencies.apply {
        // this one is included in the bot itself
        add("compileOnly", "org.jetbrains.kotlin:kotlin-stdlib")
        add("compileOnly", mikbot("api"))
        if (configurations.findByName("ksp") != null) {
            add("ksp", mikbot("plugin-processor"))
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
