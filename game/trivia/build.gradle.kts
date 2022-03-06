plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.1.6"

dependencies {
    plugin(projects.game.gameApi)
    plugin(projects.game.multipleChoiceGame)
    optionalPlugin(projects.core.gdpr)
    implementation("org.apache.commons", "commons-text", "1.9")

    // Google Translate
    implementation(platform("com.google.cloud:libraries-bom:24.4.0"))
    implementation("com.google.cloud", "google-cloud-translate")
}
