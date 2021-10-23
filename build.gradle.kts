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
    maven("https://nycode.jfrog.io/artifactory/snapshots/")
}

dependencies {
    // Uno module
    implementation(project(":uno"))

    // Bot
    implementation("dev.kord", "kord-core", "0.8.x-SNAPSHOT") {
        version {
            strictly("0.8.x-SNAPSHOT")
        }
    }
    implementation("com.kotlindiscord.kord.extensions", "kord-extensions", "1.5.1-SNAPSHOT")
    implementation("dev.kord.x", "emoji", "0.5.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.5.2")
    implementation("dev.schlaubi.lavakord", "kord", "3.0.1")
    implementation("org.litote.kmongo", "kmongo-coroutine-serialization", "4.3.0")

    // Logging
    implementation("ch.qos.logback", "logback-classic", "1.2.6")

    // Plattform support
    implementation("com.google.apis", "google-api-services-youtube", "v3-rev205-1.25.0")
    implementation("se.michaelthelin.spotify", "spotify-web-api-java", "6.5.4")

    // Verification Server
    implementation(platform("io.ktor:ktor-bom:1.6.2"))
    implementation("io.ktor", "ktor-server-netty")
    implementation("io.ktor", "ktor-locations")

    // Util
    implementation("dev.schlaubi", "envconf", "1.1")

    // SponsorBlock Client
    implementation("dev.nycode", "sponsorblock-kt", "1.0-SNAPSHOT")

    // Scheduling
    implementation("dev.inmo", "krontab", "0.6.5")
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
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.time.ExperimentalTime", "-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }
}
