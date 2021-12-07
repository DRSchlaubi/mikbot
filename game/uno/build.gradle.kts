plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.5.2")
}

kotlin {
    explicitApi()
}
