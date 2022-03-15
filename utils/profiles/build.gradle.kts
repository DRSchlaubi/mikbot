plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.1.12"

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    plugin(projects.utils.ktor)
    optionalPlugin(projects.core.gdpr)
    implementation(libs.github.repositories) {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation(libs.ktor.client.serialization)
}

mikbotPlugin {
    description.set("User profiles.")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }
}
