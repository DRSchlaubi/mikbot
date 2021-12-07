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
    implementation("dev.schlaubi", "gradle-plugin", "1.0.0")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.5.31-1.0.0")
    implementation("org.jlleitschuh.gradle", "ktlint-gradle", "10.2.0")
    implementation(gradleApi())
    implementation(localGroovy())
}
