plugins {
    `mikbot-module`
    `mikbot-publishing`
    alias(libs.plugins.kotlinx.serialization)
}

version = mikbotVersion

dependencies {
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    explicitApi()
}
