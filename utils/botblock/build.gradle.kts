plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "2.0.0"

mikbotPlugin {
    description.set("Plugin adding support to post server counts to server lists using botblock")
}
