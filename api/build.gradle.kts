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
    api(project("annotations"))
    // Bot
    api("com.kotlindiscord.kord.extensions", "kord-extensions", "1.5.2-MIKBOT.2")
    api("com.kotlindiscord.kord.extensions", "unsafe", "1.5.2-MIKBOT.2")
    api("dev.kord.x", "emoji", "0.5.0") {
        exclude("dev.kord")
    }
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.6.0")
    api("org.litote.kmongo", "kmongo-coroutine-serialization", "4.5.0")
    api("org.pf4j", "pf4j", "3.6.0")

    // Util
    api(platform("dev.schlaubi:stdx-bom:1.0.1"))
    api("dev.schlaubi", "stdx-core")
    api("dev.schlaubi", "stdx-coroutines")
    api("dev.schlaubi", "stdx-envconf")
    api("dev.schlaubi", "stdx-logging")


    // Logging
    api("ch.qos.logback", "logback-classic", "1.2.10")
}

template {
    files.add("MikBotInfo.java")
    tokens.put("VERSION", project.version)
}
