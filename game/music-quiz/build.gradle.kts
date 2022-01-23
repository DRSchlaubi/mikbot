plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.3.0"

dependencies {
    plugin(projects.game.gameApi)
    plugin(projects.music)
    plugin(projects.game.multipleChoiceGame)
    optionalPlugin(projects.core.gdpr)
}
