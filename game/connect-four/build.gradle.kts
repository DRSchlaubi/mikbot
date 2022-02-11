plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0.2"

dependencies {
    implementation(projects.game.googleEmotes)
    plugin(projects.game.gameApi)
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Connect four")
    bundle.set("connect_four")
}
