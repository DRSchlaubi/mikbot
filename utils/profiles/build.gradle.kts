plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.1.3"

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    plugin(projects.utils.ktor)
    optionalPlugin(projects.core.gdpr)
    implementation(platform("dev.nycode.github:bom:1.0.0-SNAPSHOT"))
    implementation("dev.nycode.github", "repositories") {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("io.ktor", "ktor-client-serialization")
    implementation("io.ktor", "ktor-client-logging", "1.6.2")
}

mikbotPlugin {
    description.set("User profiles.")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }
}
