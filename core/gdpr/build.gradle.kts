plugins {
    `mikbot-module`
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

mikbotPlugin {
    description.set("Plugin adding functionality to comply with the GDPR")
}
