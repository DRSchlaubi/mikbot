plugins {
    `mikbot-module`
    `mikbot-publishing`
    kotlin("plugin.serialization")
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:1.6.7"))
    implementation("io.ktor", "ktor-client-okhttp")
    implementation("io.ktor", "ktor-client-serialization")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.6.0")
}

kotlin {
    explicitApi()
}
