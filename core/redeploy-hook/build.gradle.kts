plugins {
    `mikbot-module`
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi.mikbot"
version = "2.10.0"

mikbotPlugin {
    description.set("Plugin adding a /redeploy command, backed by a webhook")
}
