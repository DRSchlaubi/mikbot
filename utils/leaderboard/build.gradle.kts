plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.11.0"

dependencies {
    optionalPlugin(projects.core.gdpr)
    optionalPlugin(projects.utils.ktor)
    implementation(libs.kmongo.id.serialization)
}

mikbotPlugin {
    description.set("Adds a leaderboard")
}

