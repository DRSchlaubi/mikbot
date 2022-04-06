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
    api(libs.lavakord.kord)

    // Plattform support
    implementation(libs.google.apis.youtube)
    api(libs.spotify)

    // SponsorBlock Client
    implementation(libs.sponsorblock)

    // Scheduling
    implementation(libs.krontab)

    // redeploy support
    optionalPlugin(projects.core.redeployHook)

    // GDPR support
    optionalPlugin(projects.core.gdpr)
}

mikbotPlugin {
    description.set("Plugin providing full music functionality for the bot")
}
