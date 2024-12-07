plugins {
    `mikbot-module`

    com.google.devtools.ksp
    alias(libs.plugins.kotlinx.serialization)
    dev.schlaubi.mikbot.`gradle-plugin`
}

dependencies {
    plugin(projects.core.ktor)
    plugin(projects.music.player)

    ktorDependency(libs.ktor.server.auth.jwt)
}

mikbotPlugin {
    pluginId = "music-api"
    description = "Adds a rest API for player functionality"
}
