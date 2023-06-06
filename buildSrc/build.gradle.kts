import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin-api", libs.versions.kotlin.get()))
    implementation(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    implementation("dev.schlaubi", "gradle-plugin", "1.0.0")
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", libs.versions.ksp.get())
    implementation("org.jlleitschuh.gradle", "ktlint-gradle", "11.4.0")
    implementation("com.github.gmazzo", "gradle-buildconfig-plugin", "3.1.0")
    implementation(gradleApi())
    implementation(localGroovy())
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_19
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
}
