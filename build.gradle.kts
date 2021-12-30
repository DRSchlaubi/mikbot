import dev.schlaubi.mikbot.gradle.removeVersion
import java.nio.file.Files

plugins {
    `mikbot-module`
    kotlin("plugin.serialization") version "1.5.31"
    application
    // This exists to add the removeVersion extension to this buildscript
    id("dev.schlaubi.mikbot.gradle-plugin") apply false
}

group = "dev.schlaubi"
version = Project.version

allprojects {
    repositories {
        maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
        mavenCentral()
        maven("https://maven.kotlindiscord.com/repository/maven-public/")
        maven("https://schlaubi.jfrog.io/artifactory/envconf/")
        maven("https://schlaubi.jfrog.io/artifactory/lavakord/")
        maven("https://nycode.jfrog.io/artifactory/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

subprojects {
    if (path != ":votebot:common") { // MPP projects don't work with ktlint
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
    }
}

dependencies {
    // Plugin system
    implementation("org.pf4j", "pf4j", "3.6.0")
    implementation("org.pf4j", "pf4j-update", "2.3.0")
    implementation("com.google.code.gson", "gson", "2.8.9")
    implementation("org.ow2.asm", "asm", "9.2") // pf4j doesn't declare a real dep on it

    sourceSets {
    }
    // Logging
    implementation("ch.qos.logback", "logback-classic", "1.2.6")

    // Util
    implementation("dev.schlaubi", "envconf", "1.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")

    implementation(project(":api"))
    implementation(kotlin("reflect"))
}

application {
    mainClass.set("dev.schlaubi.musicbot.LauncherKt")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(16))
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }

    // This is probbably the worst way of doing this,
    // but I tried to use JVM resources or compilation file manipulation for 3 hrs now with no luck
    task("exportDependencies") {
        doLast {
            val files = configurations["runtimeClasspath"].files.mapNotNull {
                it.removeVersion()
            }

            val kotlinFile = """
                package dev.schlaubi.mikbot.gradle
                
                const val transientDependencies = "${files.joinToString("\\n")}"
            """.trimIndent()

            Files.writeString(
                file("gradle-plugin/src/main/kotlin/dev/schlaubi/mikbot/gradle/TransientDependencies.kt").toPath(),
                kotlinFile
            )
        }
    }
}
