plugins {
    `mikbot-module`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}

kotlin {
    explicitApi()
}
