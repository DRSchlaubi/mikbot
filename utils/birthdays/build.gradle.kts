plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "1.8.0"

dependencies {
    plugin(projects.utils.ktor)
    optionalPlugin(projects.core.gdpr)
}
