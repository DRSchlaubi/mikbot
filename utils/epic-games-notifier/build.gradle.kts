plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "3.2.2"

dependencies {
    plugin(projects.utils.ktor)
}
