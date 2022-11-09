plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "1.7.1"

dependencies {
    plugin(projects.utils.ktor)
    optionalPlugin(projects.core.gdpr)
}
