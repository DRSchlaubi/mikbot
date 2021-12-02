plugins {
    `mikbot-module`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:1.6.6"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.1")
    implementation("io.ktor", "ktor-client-okhttp")
    implementation("io.ktor", "ktor-client-serialization")
}

kotlin {
    explicitApi()
}
