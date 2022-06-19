plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "3.3.0"

dependencies {
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Plugin providing core APIs for all games")
    bundle.set("games")
}
