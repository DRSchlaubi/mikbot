plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.2.0"

dependencies {
    plugin(projects.game.gameApi)
    implementation(projects.game.uno)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Plugin adding functionality to play UNO on Discord")
}
