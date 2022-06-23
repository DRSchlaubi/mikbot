plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.3.4"

dependencies {
    api(libs.lavakord.kord)

    // Plattform support
    implementation(libs.google.apis.youtube)
    api(libs.spotify)

    // SponsorBlock Client
    implementation(projects.utils.sponsorblockKt)

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
