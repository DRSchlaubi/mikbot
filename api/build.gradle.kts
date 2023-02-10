plugins {
    `mikbot-module`
    `mikbot-publishing`
    `mikbot-template`
    java
}

group = "dev.schlaubi.mikbot"
version = Project.version + "-SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
    // Api base
    api(projects.api.annotations)
    // Bot
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
    className.set("MikBotInfo")
    packageName.set("dev.schlaubi.mikbot.plugin.api")
}
