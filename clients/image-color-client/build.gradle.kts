plugins {
    `mikbot-module`
    `mikbot-publishing`
    alias(libs.plugins.kotlinx.serialization)
}

group = "dev.nycode"
version = "1.1.0"

dependencies {
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}

kotlin {
    explicitApi()
}