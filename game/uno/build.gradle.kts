plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "1.2.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}
