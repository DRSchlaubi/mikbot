import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `mikbot-module`
    `mikbot-publishing`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"

dependencies {
    api(libs.lavakord.kord)
    api(libs.lavakord.sponsorblock)
    api(libs.lavakord.lavsrc)

    // Plattform support
    implementation(libs.google.apis.youtube)
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
    pluginId = "music-player"
    description = "Plugin providing full music functionality for the bot"
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "mikbot-music-player"
        }
    }
}
