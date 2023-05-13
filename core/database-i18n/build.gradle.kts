plugins {
    `mikbot-module`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

dependencies {
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Implementation of the bots i18n-system backed by a database")
}
