plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.7.0"

dependencies {
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Implementation of the bots i18n-system backed by a database")
}
