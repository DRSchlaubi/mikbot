plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.11.0"

dependencies {
    plugin(projects.utils.ktor)
}

mikbotPlugin {
    description.set("Plugin requiring each invite of the bot to be manually confirmed by an owner")
}
