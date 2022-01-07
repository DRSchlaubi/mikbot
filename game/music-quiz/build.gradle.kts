plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.1.0"

dependencies {
    plugin(project(":game:game-api"))
    plugin(project(":music"))
    optionalPlugin(project(":core:gdpr"))
}
