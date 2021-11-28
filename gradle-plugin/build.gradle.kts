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
