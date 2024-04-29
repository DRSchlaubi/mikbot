plugins {
    `mikbot-module`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

dependencies {
    plugin(projects.music.player)
    optionalPlugin(projects.core.gdpr)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

mikbotPlugin {
    pluginId = "music-commands"
    description = "Plugin providing full music related commands"
}
