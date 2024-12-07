plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kord.common)
                api(libs.lavalink.protocol)
                implementation(libs.ktor.resources)
                implementation(libs.kotlinx.serialization.json)
                api(libs.lavakord.sponsorblock)
            }
        }
    }
}
