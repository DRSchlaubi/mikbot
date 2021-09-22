plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    application
}

group = "dev.schlaubi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven("https://schlaubi.jfrog.io/artifactory/lavakord/")
}

dependencies {
    implementation("com.kotlindiscord.kord.extensions", "kord-extensions", "1.5.0-SNAPSHOT")
    implementation("dev.schlaubi", "envconf", "1.1")
    implementation("dev.schlaubi.lavakord", "kord", "2.0.2")
    implementation("ch.qos.logback", "logback-classic", "1.2.6")
    implementation("org.litote.kmongo", "kmongo-coroutine-serialization", "4.3.0")
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
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}
