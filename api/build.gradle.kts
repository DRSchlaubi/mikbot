plugins {
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0-SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
    // Api base
    api("com.kotlindiscord.kord.extensions", "kord-extensions", "1.5.1-SNAPSHOT")
    api("dev.kord.x", "emoji", "0.5.0")
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.5.2")
    api("org.litote.kmongo", "kmongo-coroutine-serialization", "4.3.0")
    api("org.pf4j", "pf4j", "3.6.0")

    // Util
    api("dev.schlaubi", "envconf", "1.1")

    // Logging
    api("ch.qos.logback", "logback-classic", "1.2.6")
}
