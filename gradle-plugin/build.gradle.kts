plugins {
    id("com.gradle.plugin-publish") version "0.20.0"
    `java-gradle-plugin`
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
}

group = "dev.schlaubi"
version = "2.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.6.1")
    compileOnly(kotlin("gradle-plugin"))
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

    description = "Utility plugin to build Mikbot and PF4J plugins"

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
