plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.3.8"

dependencies {
    plugin(projects.game.gameApi)
    plugin(projects.music)
    plugin(projects.game.multipleChoiceGame)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Plugin providing Song Quizzes")
    bundle.set("song_quiz")
}
