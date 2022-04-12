plugins {
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.serialization)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    explicitApi()
}
