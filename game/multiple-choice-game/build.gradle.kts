plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "1.2.8"

dependencies {
    plugin(projects.game.gameApi)
}

mikbotPlugin {
    bundle.set("multiple_choice")
}
