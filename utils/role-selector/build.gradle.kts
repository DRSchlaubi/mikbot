plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.0.2"

mikbotPlugin {
    description.set("Give Roles on a specific Event")
}
