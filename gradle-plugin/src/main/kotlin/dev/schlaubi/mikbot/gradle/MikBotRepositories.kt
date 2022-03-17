package dev.schlaubi.mikbot.gradle

import org.gradle.api.Project

fun Project.addRepositories() {
    repositories.mavenCentral()
    repositories.maven {
        it.name = "Mikbot"
        it.url = uri("https://schlaubi.jfrog.io/artifactory/mikbot/")
    }
    repositories.maven {
        it.name = "Envconf"
        it.url = uri("https://schlaubi.jfrog.io/artifactory/envconf/")
    }
    repositories.maven {
        it.name = "Kotlin Discord"
        it.url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}
