plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "1.2.0"

dependencies {
    plugin(projects.utils.ktor)
}
