plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}
