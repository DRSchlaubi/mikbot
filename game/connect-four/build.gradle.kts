plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "2.0.1"

dependencies {
    implementation(projects.game.googleEmotes)
    plugin(projects.game.gameApi)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Connect four")
    bundle.set("connect_four")
}
