plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "1.0.10"

dependencies {
    plugin(projects.utils.ktor)
}
