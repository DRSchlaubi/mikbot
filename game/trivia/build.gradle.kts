plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.4.0"

dependencies {
    plugin(projects.game.gameApi)
    plugin(projects.game.multipleChoiceGame)
    optionalPlugin(projects.core.gdpr)
    implementation(libs.commons.text)

    // Google Translate
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.translate)
}
