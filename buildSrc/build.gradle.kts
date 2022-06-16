plugins {
    groovy
    `kotlin-dsl`
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.6.21"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("dev.schlaubi", "gradle-plugin", "1.0.0")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.6.21-1.0.6")
    implementation("org.jlleitschuh.gradle", "ktlint-gradle", "10.3.0")
    implementation(gradleApi())
    implementation(localGroovy())
}
