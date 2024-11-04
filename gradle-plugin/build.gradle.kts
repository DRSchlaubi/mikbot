import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.gradle.publish)
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

group = "dev.schlaubi.mikbot"
version = libs.versions.api.get()

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://releases-repo.kordex.dev")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.jdk8)
    implementation(libs.kordex.gradle.plugin)
    implementation(libs.gradle.license.report)
    compileOnly(kotlin("gradle-plugin"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
        jvmTarget = JvmTarget.JVM_17
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("mikbot-plugin-gradle-plugin") {
            id = "dev.schlaubi.mikbot.gradle-plugin"
            implementationClass = "dev.schlaubi.mikbot.gradle.MikBotPluginGradlePlugin"
            displayName = "Mikbot Gradle Plugin"
            description = "Utility plugin to build Mikbot and PF4J plugins"
            tags = setOf("mikbot", "pf4j", "plugins", "kotlin")
        }
    }

    website = "https://github.com/DRSchlaubi/mikbot/tree/main/gradle-plugin"
    vcsUrl = "https://github.com/DRSchlaubi/mikbot"

}

afterEvaluate {
    buildConfig {
        packageName("dev.schlaubi.mikbot.gradle")
        className("MikBotPluginInfo")
        buildConfigField("String", "VERSION", "\"${libs.versions.api.get()}\"")
        buildConfigField("String", "KORDEX_VERSION", "\"${libs.versions.kordex.asProvider().get()}\"")
        buildConfigField("boolean", "IS_MIKBOT", (System.getenv("BUILD_PLUGIN_CI")?.toBoolean() != true).toString())
    }
}
