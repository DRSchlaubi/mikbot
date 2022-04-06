plugins {
    `mikbot-plugin`
    groovy
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.7.0"

dependencies {
    api("dev.schlaubi.lavakord", "kord", "3.5.1")

    // Plattform support
    implementation("com.google.apis", "google-api-services-youtube", "v3-rev20210915-1.32.1")
    api("se.michaelthelin.spotify", "spotify-web-api-java", "7.0.0")

    // SponsorBlock Client
    implementation("dev.nycode", "sponsorblock-kt", "1.0-SNAPSHOT")

    // Scheduling
    implementation("dev.inmo", "krontab", "0.7.1")

    // redeploy support
    optionalPlugin(project(":core:redeploy-hook"))

    // GDPR support
    optionalPlugin(project(":core:gdpr"))
}

mikbotPlugin {
    description.set("Plugin providing full music functionality for the bot")
}
