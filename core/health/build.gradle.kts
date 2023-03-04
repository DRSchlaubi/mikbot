plugins {
    org.jetbrains.kotlin.jvm
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = "1.7.0"

dependencies {
    plugin(projects.core.ktor)
}

mikbotPlugin {
    description.set("Plugin providing an /healthz endpoint used for health checking.")
}
