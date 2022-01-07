plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0.5"

dependencies {
    plugin(project(":game:game-api"))
    plugin(project(":music"))
    optionalPlugin(project(":core:gdpr"))
}
