import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin-api", "1.8.10"))
    implementation(kotlin("gradle-plugin", "1.8.10"))
    implementation("dev.schlaubi", "gradle-plugin", "1.0.0")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.8.10-1.0.9")
    implementation("org.jlleitschuh.gradle", "ktlint-gradle", "11.1.0")
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
