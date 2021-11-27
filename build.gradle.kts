import java.nio.file.Files
import java.nio.file.Path

plugins {
    `mikbot-module`
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    application
}

group = "dev.schlaubi"
version = Project.version

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.kotlindiscord.com/repository/maven-public/")
        maven("https://schlaubi.jfrog.io/artifactory/envconf/")
        maven("https://schlaubi.jfrog.io/artifactory/lavakord/")
        maven("https://nycode.jfrog.io/artifactory/snapshots/")
    }
}

dependencies {

    implementation("org.pf4j", "pf4j", "3.6.0")
    implementation("org.pf4j", "pf4j-update", "2.3.0")
    implementation("com.google.code.gson", "gson", "2.8.9")
    implementation("org.ow2.asm", "asm", "9.2") // pf4j doesn't declare a real dep on it

    // Logging
    implementation("ch.qos.logback", "logback-classic", "1.2.6")

    // Util
    implementation("dev.schlaubi", "envconf", "1.1")

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

    task("exportDependencies") {
        doLast {
            val files = configurations["runtimeClasspath"].files.mapNotNull { it.removeVersion() }
            Files.writeString(Path.of("main-dependency-export.txt"), files.joinToString("\n"))
        }
    }
}

ktlint {
    disabledRules.add("no-wildcard-imports")
}

fun File.removeVersion(): String? {
    val possibleVersions = name.split("-[0-9]".toRegex())
    if (possibleVersions.size <= 1) return null
    val version = possibleVersions.last()

    return name.substring(0, name.length - version.length - 2)
}
