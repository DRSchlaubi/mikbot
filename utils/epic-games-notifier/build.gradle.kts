plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi"
version = "1.0.1"

dependencies {
    plugin(project(":utils:ktor"))
}
