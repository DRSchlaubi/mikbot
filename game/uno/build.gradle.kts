plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "1.3.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}
