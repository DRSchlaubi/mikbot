plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "1.2.1"

dependencies {
    plugin(projects.game.gameApi)
}
