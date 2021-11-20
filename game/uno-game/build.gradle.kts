plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.1.1"

dependencies {
    plugin(project(":game:game-api"))
    implementation(project(":game:uno"))
}

dependencies {
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Plugin adding functionality to play UNO on Discord")
}
