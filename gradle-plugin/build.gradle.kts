import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "1.0.0-rc-3"
    `java-gradle-plugin`
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "dev.schlaubi"
version = "2.5.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.3")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.6.3")
    compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        create("mikbot-plugin-gradle-plugin") {
            id = "dev.schlaubi.mikbot.gradle-plugin"
            implementationClass = "dev.schlaubi.mikbot.gradle.MikBotPluginGradlePlugin"
            displayName = "Mikbot Gradle Plugin"
        }
    }
}


pluginBundle {
    website = "https://github.com/DRSchlaubi/mikbot/tree/main/gradle-plugin"
    vcsUrl = "https://github.com/DRSchlaubi/mikbot"

    description = "Utility plugin to build Mikbot and PF4J plugins"
    tags = listOf("mikbot", "pf4j", "plugins", "kotlin")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }
}
