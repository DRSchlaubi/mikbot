plugins {
    `mikbot-module`
    org.jetbrains.kotlin.jvm
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = "2.11.0"

mikbotPlugin {
    description.set("Plugin changing the bots presence every 30 seconds")
}
