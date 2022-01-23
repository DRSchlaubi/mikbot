plugins {
    `mikbot-plugin`
    groovy
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.2.4"

dependencies {
    api("dev.schlaubi.lavakord", "kord", "3.1.1")

    // Plattform support
    implementation("com.google.apis", "google-api-services-youtube", "v3-rev205-1.25.0")
    api("se.michaelthelin.spotify", "spotify-web-api-java", "6.5.4")

    // SponsorBlock Client
    implementation("dev.nycode", "sponsorblock-kt", "1.0-SNAPSHOT")

    // Scheduling
    implementation("dev.inmo", "krontab", "0.6.5")

    // redeploy support
    optionalPlugin(project(":core:redeploy-hook"))

    // GDPR support
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Plugin providing full music functionality for the bot")
}
