plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "3.4.0"

dependencies {
    plugin(projects.utils.ktor)
}
