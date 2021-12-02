plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi()

    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
            }
        }
    }
}
