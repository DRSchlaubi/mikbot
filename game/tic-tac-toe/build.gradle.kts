plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "2.7.0"

dependencies {
    plugin(projects.game.gameApi)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Probably the most inefficient implementation of Tic Tac Toe, but JVM is fast, so it doesn't matter")
    bundle.set("tic_tac_toe")
}
