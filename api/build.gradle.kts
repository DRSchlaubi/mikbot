plugins {
    `mikbot-module`
    `mikbot-publishing`
    `mikbot-template`
    java
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

kotlin {
    explicitApi()
}

dependencies {
    // Api base
    api(projects.api.annotations)
    // Bot
    api("dev.kord:kord-common-jvm:${libs.versions.kord.get()}")
    api("dev.kord:kord-rest-jvm:${libs.versions.kord.get()}")
    api("dev.kord:kord-gateway-jvm:${libs.versions.kord.get()}")
    api("dev.kord:kord-core-jvm:${libs.versions.kord.get()}")
    api(libs.kordex)
    api(libs.kordex.unsafe)
    api(libs.kordx.emoji) {
        exclude("dev.kord")
    }
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.kmongo.coroutine.serialization)
    api(libs.pf4j)

    // Util
    api(libs.stdx.full)

    // Logging
    api(libs.logback.classic)
}

template {
    className = "MikBotInfo"
    packageName = "dev.schlaubi.mikbot.plugin.api"
}
