plugins {
    `mikbot-module`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

dependencies {
    plugin(projects.core.ktor)
}

mikbotPlugin {
    description = "Plugin providing an /healthz endpoint used for health checking."
}
