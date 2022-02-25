plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.1.4"

dependencies {
    implementation(projects.game.googleEmotes)
    plugin(projects.game.gameApi)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Hangman but with family friendly")
}
