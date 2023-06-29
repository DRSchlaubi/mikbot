plugins {
    `mikbot-module`
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

mikbotPlugin {
    description = "Plugin adding a /redeploy command, backed by a webhook"
}
