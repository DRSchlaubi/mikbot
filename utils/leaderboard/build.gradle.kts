plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.0.4"

dependencies {
    optionalPlugin(projects.core.gdpr)
    optionalPlugin(projects.utils.ktor)
    implementation("org.litote.kmongo", "kmongo-id-serialization", "4.4.0")
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
