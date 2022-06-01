plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "2.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}
