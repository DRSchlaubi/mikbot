import org.gradle.kotlin.dsl.get
import java.nio.file.Files

plugins {
    `mikbot-module`
    alias(libs.plugins.kotlinx.serialization)
    application
    // This exists to add the removeVersion extension to this buildscript
    dev.schlaubi.mikbot.`gradle-plugin` apply false
    `mikbot-template`
}

group = "dev.schlaubi"
version = libs.versions.api.get()

allprojects {
    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    // Plugin system
    implementation(libs.pf4j)
    implementation(libs.pf4j.update)
    implementation(libs.gson)
    implementation(libs.asm) // pf4j doesn't declare a real dep on it
    implementation(libs.koin)
    implementation("dev.schlaubi:gradle-plugin") {
        // otherwise we would add entire Gradle in here
        isTransitive = false
    }
    implementation("dev.kord:kord-common-jvm:feature-user-apps-20240925.194307-6")
    implementation("dev.kord:kord-rest-jvm:feature-user-apps-20240925.194307-6")

    // Util
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)

    implementation(projects.api)
    implementation(kotlin("reflect"))
}

application {
    mainClass = "dev.schlaubi.musicbot.LauncherKt"
    applicationName = "mikmusic"
    applicationDefaultJvmArgs = listOf("--enable-preview")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
    }
}

tasks {
    startScripts {
        classpath = DummyFileCollection(listOf("lib/*", "lib/."))
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
        archiveBaseName = "bot"
        archiveExtension = "tar.gz"
    }

    task<Copy>("installCi") {
        dependsOn(distTar)
        from(distTar)
        include("*.tar.gz")
        into("ci-repo/$version")
    }

    val installPlugins = register<Copy>("installPlugins") {
        (project.file("plugins.txt")
            .takeIf { it.exists() } ?: return@register)
            .readLines()
            .asSequence()
            .filterNot { it.isBlank() || it.startsWith('#') }
            .map {
                findProject(it) ?: error("Project '$it' not found, make sure to reference it from the root project")
            }
            .forEach {
                from(it.tasks.findByPath("assemblePlugin") ?: error("Project ${it.path} does not have a plugin task"))
            }
        into(rootProject.file("plugins"))
    }

    classes {
        dependsOn(installPlugins)
    }
}

template {
    className = "MikBotInfo"
    packageName = "dev.schlaubi.mikbot.plugin.api"
}
