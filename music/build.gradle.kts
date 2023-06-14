import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `mikbot-module`
    `mikbot-publishing`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = "2.22.0-SNAPSHOT"

dependencies {
    api(libs.lavakord.kord)

    // Plattform support
    implementation(libs.google.apis.youtube)
    api(libs.spotify)

    // SponsorBlock Client
    implementation(projects.clients.sponsorblockKt)

    // Scheduling
    implementation(libs.krontab)

    // redeploy support
    optionalPlugin(projects.core.redeployHook)

    // GDPR support
    optionalPlugin(projects.core.gdpr)

    // Image Color Client
    api(projects.clients.imageColorClient)
    api(projects.clients.imageColorClientKord)

    implementation(libs.ktor.client.logging)
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
