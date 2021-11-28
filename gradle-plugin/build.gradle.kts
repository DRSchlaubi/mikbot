import org.apache.tools.ant.filters.ReplaceTokens
import java.nio.file.Files

plugins {
    id("com.gradle.plugin-publish") version "0.16.0"
    `java-gradle-plugin`
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
}

group = "dev.schlaubi"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.1")
}

tasks {
    val sourcesForRelease = task<Copy>("sourcesForRelease") {
        val exportFile = rootDir.parentFile.toPath().resolve("main-dependency-export.txt")
        val exists = Files.exists(exportFile)
        if (!exists) {
            logger.warn("main-dependency-export.txt not found, consider running ./gradlew exportDependencies")
        }
        val dependencies = if (exists) {
            Files.readString(exportFile).replace("\n", "\\n")
        } else {
            ""
        }

        from("src/main/java") {
            include("**/TransientDependencies.java")
            val tokens = mapOf("transient_dependencies" to dependencies)
            filter<ReplaceTokens>(mapOf("tokens" to tokens))
        }
        into("build/filteredSrc")
        includeEmptyDirs = false
    }

    compileJava {
        dependsOn(sourcesForRelease)

        source(sourcesForRelease.destinationDir)
    }
}

sourceSets {
    main {
        java {
            // provided by sourcesForRelease task
            exclude("**/TransientDependencies.java")
        }
    }
}

gradlePlugin {
    plugins {
        create("mikbot-plugin-gradle-plugin") {
            id = "dev.schlaubi.mikbot.gradle-plugin"
            implementationClass = "dev.schlaubi.mikbot.gradle.MikBotPluginGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/DRSchlaubi/mikbot/tree/main/gradle-plugin"
    vcsUrl = "https://github.com/DRSchlaubi/mikbot"

    description = "Automatically download your dependencies at runtime on Spigot 1.16.5+"

    (plugins) {
        "mikbot-plugin-gradle-plugin" {
            displayName = "Mikbot Gradle plugin"
            description = this@pluginBundle.description
            tags = listOf("kotlin", "discord", "plugins")
            version = project.version.toString()
        }
    }

    mavenCoordinates {
        groupId = "dev.schlaubi"
        artifactId = "mikbot-gradle-plugin"
        version = project.version.toString()
    }
}
