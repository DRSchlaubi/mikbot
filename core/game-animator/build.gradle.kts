plugins {
    `mikbot-module`
    org.jetbrains.kotlin.jvm
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

mikbotPlugin {
    description = "Plugin changing the bots presence every 30 seconds"
}
