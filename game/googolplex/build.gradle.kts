plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "2.4.0"

dependencies {
    plugin(projects.game.gameApi)
    implementation(projects.game.googleEmotes)
}

mikbotPlugin {
    description.set("My version of mastermind")
}
