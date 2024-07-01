plugins {
    `mikbot-module`
    `mikbot-publishing`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
    `jvm-test-suite`
}

group = "dev.schlaubi.mikbot"

dependencies {
    implementation(projects.api)
    api(libs.lavakord.kord)
    api(libs.lavakord.sponsorblock)
    api(libs.lavakord.lavsrc)
    api(libs.lavakord.lavasearch)
    api(libs.lavakord.lyrics)

    // Plattform support
    implementation(libs.google.apis.youtube)

    // redeploy support
    optionalPlugin(projects.core.redeployHook)

    // GDPR support
    optionalPlugin(projects.core.gdpr)

    // Image Color Client
    api(projects.clients.imageColorClient)
    api(projects.clients.imageColorClientKord)

    implementation(libs.ktor.client.logging)

    testImplementation(kotlin("test-junit5"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }
    }
}

mikbotPlugin {
    pluginId = "music-player"
    description = "Plugin providing full music functionality for the bot"
    bundle = "music"
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "mikbot-music-player"
        }
    }
}
