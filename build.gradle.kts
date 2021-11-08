plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    application
}

group = "dev.schlaubi"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.kotlindiscord.com/repository/maven-public/")
        maven("https://schlaubi.jfrog.io/artifactory/envconf/")
        maven("https://schlaubi.jfrog.io/artifactory/lavakord/")
        maven("https://nycode.jfrog.io/artifactory/snapshots/")
    }
}

dependencies {

    implementation("org.pf4j", "pf4j", "3.6.0")
    implementation("org.ow2.asm", "asm", "9.2") // pf4j doesn't declare a real dep on it

    // Logging
    implementation("ch.qos.logback", "logback-classic", "1.2.6")

    // Util
    implementation("dev.schlaubi", "envconf", "1.1")


    implementation(project(":api"))
}

application {
    mainClass.set("dev.schlaubi.musicbot.LauncherKt")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(16))
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xopt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }
}
