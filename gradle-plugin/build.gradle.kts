import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.gradle.publish)
    `java-gradle-plugin`
    alias(libs.plugins.kotlinx.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

group = "dev.schlaubi"
version = libs.versions.api

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.jdk8)
    compileOnly(kotlin("gradle-plugin"))
}

kotlin {
    jvmToolchain(19)
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
            freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }
}

afterEvaluate {
    buildConfig {
        packageName("dev.schlaubi.mikbot.gradle")
        className("MikBotPluginInfo")
        buildConfigField("String", "VERSION", "\"${libs.versions.api}\"")
        buildConfigField("boolean", "IS_MIKBOT", "true")
    }
}

