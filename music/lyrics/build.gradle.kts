plugins {
    `mikbot-module`
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    plugin(projects.music.player)
    plugin(projects.core.ktor)
    ktorDependency(libs.ktor.server.cors)
}

mikbotPlugin {
    pluginId = "music-lyrics"
    description = "Plugin providing lyrics for the music plugin"
    bundle = "lyrics"
}
