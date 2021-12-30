plugins {
    groovy
    `kotlin-dsl`
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("dev.schlaubi", "gradle-plugin", "1.0.0")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.6.10-1.0.2")
    implementation("org.jlleitschuh.gradle", "ktlint-gradle", "10.2.1")
    implementation(gradleApi())
    implementation(localGroovy())
}
