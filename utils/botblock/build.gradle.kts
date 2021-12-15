plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

mikbotPlugin {
    description.set("Plugin adding support to post server counts to server lists using botblock")
}
