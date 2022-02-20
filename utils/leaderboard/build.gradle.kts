plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.0.0"

mikbotPlugin {
    description.set("Adds a leaderboarder")
}
