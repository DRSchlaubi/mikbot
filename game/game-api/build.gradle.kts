plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.0.9"

dependencies {
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Plugin providing core APIs for all games")
}
