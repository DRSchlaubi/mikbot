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
    api("com.kotlindiscord.kord.extensions", "kord-extensions", "1.5.2-SNAPSHOT")
    api("com.kotlindiscord.kord.extensions", "unsafe", "1.5.2-SNAPSHOT")
    api("dev.kord.x", "emoji", "0.5.0") {
        exclude("dev.kord")
    }
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.6.0")
    api("org.litote.kmongo", "kmongo-coroutine-serialization", "4.5.0")
    api("org.pf4j", "pf4j", "3.6.0")

    // Util
    api("dev.schlaubi", "envconf", "1.1")

    // Logging
    api("ch.qos.logback", "logback-classic", "1.2.10")
}

template {
    files.add("MikBotInfo.java")
    tokens.put("VERSION", project.version)
}
