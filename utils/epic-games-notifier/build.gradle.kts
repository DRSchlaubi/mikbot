plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "2.1.0"

dependencies {
    plugin(projects.utils.ktor)
}
