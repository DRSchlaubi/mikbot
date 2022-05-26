plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "3.0.1"

dependencies {
    plugin(projects.utils.ktor)
}
