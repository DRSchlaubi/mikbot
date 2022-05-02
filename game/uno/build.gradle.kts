plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "2.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}
