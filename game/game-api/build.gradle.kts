plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.1.8"

dependencies {
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Plugin providing core APIs for all games")
    bundle.set("games")
}
