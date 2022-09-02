plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.2.0"

dependencies {
    plugin(projects.utils.ktor)
}

mikbotPlugin {
    description.set("Plugin providing an /healthz endpoint used for health checking.")
}
