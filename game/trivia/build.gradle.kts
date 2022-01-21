plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

dependencies {
    plugin(projects.game.gameApi)
    plugin(projects.game.multipleChoiceGame)
    optionalPlugin(projects.core.gdpr)
    implementation("org.apache.commons", "commons-text", "1.9")

    // Google Translate
    implementation(platform("com.google.cloud:libraries-bom:20.9.0"))
    implementation("com.google.cloud", "google-cloud-translate")
}
