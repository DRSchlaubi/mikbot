import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "2.11.0"

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

    // Image Color Client
    api(projects.utils.imageColorClient)
    api(projects.utils.imageColorClientKord)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }
}

mikbotPlugin {
    description.set("Plugin providing full music functionality for the bot")
}
