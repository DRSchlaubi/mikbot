plugins {
    groovy
    `kotlin-dsl`
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.5.31-1.0.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.0")
    implementation(gradleApi())
    implementation(localGroovy())
}
