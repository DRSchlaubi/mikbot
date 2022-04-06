plugins {
    `mikbot-module`
    `mikbot-publishing`
    `mikbot-template`
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
    api(platform("dev.schlaubi:stdx-bom:1.0.1"))
    api("dev.schlaubi", "stdx-core")
    api("dev.schlaubi", "stdx-coroutines")
    api("dev.schlaubi", "stdx-envconf")
    api("dev.schlaubi", "stdx-logging")


    // Logging
    api(libs.logback.classic)
}

template {
    files.add("MikBotInfo.java")
    tokens.put("VERSION", project.version)
}
