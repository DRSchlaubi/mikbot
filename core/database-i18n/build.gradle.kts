plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.1.5"

dependencies {
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Implementation of the bots i18n-system backed by a database")
}
