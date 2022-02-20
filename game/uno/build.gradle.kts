plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "1.2.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.6.0")
}

kotlin {
    explicitApi()
}
