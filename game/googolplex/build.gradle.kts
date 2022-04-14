plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "2.0.1"

dependencies {
    plugin(projects.game.gameApi)
    implementation(projects.game.googleEmotes)
}

mikbotPlugin {
    description.set("My version of mastermind")
}
