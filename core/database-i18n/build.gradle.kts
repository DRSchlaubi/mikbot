plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = Project.version

dependencies {
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Implementation of the bots i18n-system backed by a database")
}
