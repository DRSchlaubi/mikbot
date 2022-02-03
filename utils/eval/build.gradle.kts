plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0.0"

dependencies {
    implementation("org.mozilla", "rhino", "1.7.14")
}

mikbotPlugin {
    description.set("Plugin allowing users to execute code.")
}
