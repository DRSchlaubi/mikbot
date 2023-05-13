import java.nio.file.Files

plugins {
    `mikbot-module`
    kotlin("plugin.serialization") version "1.7.22"
    application
    // This exists to add the removeVersion extension to this buildscript
    id("dev.schlaubi.mikbot.gradle-plugin") apply false
    `mikbot-publishing`
    `mikbot-template`
}

group = "dev.schlaubi"
version = Project.version + "-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.kotlindiscord.com/repository/maven-public/")
        maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
        maven("https://schlaubi.jfrog.io/artifactory/envconf/")
        maven("https://schlaubi.jfrog.io/artifactory/lavakord/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

subprojects {
    if (path != ":votebot:common") { // MPP projects don't work with ktlint
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
    }
}

dependencies {

    // Plugin system
    implementation(libs.pf4j)
    implementation(libs.pf4j.update)
    implementation(libs.gson)
    implementation(libs.asm) // pf4j doesn't declare a real dep on it

    implementation(libs.koin)

    // Util
    implementation(libs.kotlinx.serialization.json)

    implementation(projects.api)
    implementation(kotlin("reflect"))
}

application {
    mainClass.set("dev.schlaubi.musicbot.LauncherKt")
}

tasks {
    startScripts {
        classpath = DummyFileCollection(listOf("lib/*", "lib/."))
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }

    // This is probably the worst way of doing this,
    // but I tried to use JVM resources or compilation file manipulation for 3 hrs now with no luck
    task("exportDependencies") {
        doLast {
            val deps = configurations["runtimeClasspath"].resolvedConfiguration.resolvedArtifacts.mapNotNull {
                val idWithoutPlatform = it.moduleVersion.id.name.substringBefore("-jvm")
                it.moduleVersion.id.group + ":" + idWithoutPlatform
            }

            val kotlinFile = """
                package dev.schlaubi.mikbot.gradle

                const val transientDependencies = "${deps.joinToString("\\n")}"
            """.trimIndent()

            Files.writeString(
                rootProject.file("gradle-plugin/src/main/kotlin/dev/schlaubi/mikbot/gradle/TransientDependencies.kt")
                    .toPath(),
                kotlinFile
            )
        }
    }

    distTar {
        compression = Compression.GZIP
        archiveExtension.set("tar.gz")
    }

    task<Copy>("installCi") {
        dependsOn(distTar)
        from(distTar)
        include("*.tar.gz")
        into("ci-repo/$version")
    }

    val installPlugins = task<Copy>("installPlugins") {
        (project.file("plugins.txt")
            .takeIf { it.exists() } ?: return@task)
            .readLines()
            .asSequence()
            .filterNot { it.isBlank() || it.startsWith('#') }
            .map {
                findProject(it) ?: error("Project '$it' not found, make sure to reference it from the root project")
            }
            .forEach {
                from(it.tasks["assemblePlugin"] ?: error("Project $path does not have a plugin task"))
            }
        into(rootProject.file("plugins"))
    }

    classes {
        dependsOn(installPlugins)
    }
}

template {
    className.set("MikBotInfo")
    packageName.set("dev.schlaubi.mikbot.plugin.api")
}
