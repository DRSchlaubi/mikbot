plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.0.0"

dependencies {
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Plugin providing core APIs for all games")
}
