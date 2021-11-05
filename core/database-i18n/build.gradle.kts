plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(project(":core:gdpr"))
}
