plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.0.1"

dependencies {
    optionalPlugin(projects.core.gdpr)
    optionalPlugin(projects.utils.ktor)
    implementation(libs.kmongo.id.serialization)
}

mikbotPlugin {
    description.set("Adds a leaderboard")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }
}
