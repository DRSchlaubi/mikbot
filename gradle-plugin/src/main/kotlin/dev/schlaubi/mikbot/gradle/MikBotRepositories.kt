package dev.schlaubi.mikbot.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

fun Project.addRepositories() {
    repositories.mavenCentral()
    repositories.maven {
        it.name = "Mikbot"
        it.url = uri("https://schlaubi.jfrog.io/artifactory/mikbot/")
    }
    repositories.maven {
        it.name = "Envconf"
        it.url = uri("https://schlaubi.jfrog.io/artifactory/envconf/")
    }
    repositories.maven {
        it.name = "Kotlin Discord"
        it.url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
    repositories.maven {
        it.name = "Sonatype Snapshots"
        it.url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    repositories.maven {
        it.name = "Sonatype Snapshots"
        it.url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

fun Project.addDependencies() {
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

private fun DependencyHandler.mikbot(module: String): Any =
    if (MikBotPluginInfo.IS_MIKBOT) project(mapOf("path" to ":$module")) else "dev.schlaubi:mikbot-$module:${MikBotPluginInfo.VERSION}"
