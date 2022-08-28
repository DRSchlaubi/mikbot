import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    `kotlin-dsl`
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("dev.schlaubi", "gradle-plugin", "1.0.0")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.7.10-1.0.6")
    implementation("org.jlleitschuh.gradle", "ktlint-gradle", "11.0.0")
    implementation("com.github.gmazzo", "gradle-buildconfig-plugin", "3.1.0")
    implementation(gradleApi())
    implementation(localGroovy())
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            languageVersion = "1.5" // gradle is slow at updating kotlin
        }
    }
}
