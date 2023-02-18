plugins {
    org.jetbrains.kotlin.jvm
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = "2.9.0"

mikbotPlugin {
    description.set("Plugin adding functionality to comply with the GDPR")
}
