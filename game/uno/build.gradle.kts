plugins {
    kotlin("jvm")
}

group = "dev.schlaubi"
version = "1.0.1"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}
