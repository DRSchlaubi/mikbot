plugins {
    `mikbot-module`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.serialization)
}

kotlin {
    explicitApi()
}
