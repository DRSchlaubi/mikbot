plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "1.2.6"

dependencies {
    plugin(projects.game.gameApi)
}
