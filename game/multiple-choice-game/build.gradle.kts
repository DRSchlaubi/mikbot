plugins {
    `mikbot-module`
    `mikbot-plugin`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "1.0.0"

dependencies {
    plugin(projects.game.gameApi)
}
