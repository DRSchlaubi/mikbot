plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.9.1"

dependencies {
    plugin(projects.utils.ktor)
}

mikbotPlugin {
    description.set("Plugin requiring each invite of the bot to be manually confirmed by an owner")
}
