plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.4.2"

mikbotPlugin {
    description.set("Give Roles on a specific Event")
    bundle.set("roleselector")
}
