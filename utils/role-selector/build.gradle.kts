plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.6.0"

mikbotPlugin {
    description.set("Give Roles on a specific Event")
    bundle.set("roleselector")
}
