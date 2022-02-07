plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0.7"

dependencies {
    plugin(projects.game.gameApi)
    implementation(projects.game.googleEmotes)
}

mikbotPlugin {
    description.set("My version of mastermind")
}
