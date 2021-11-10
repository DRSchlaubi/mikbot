plugins {
    `mikbot-plugin`
    `mikbot-module`
    kotlin("plugin.serialization")
}

group = "dev.schlaubi.mikbot"
version = "1.0.1"

dependencies {
    // Verification Server
    implementation(platform("io.ktor:ktor-bom:1.6.2"))
    implementation("io.ktor", "ktor-server-netty")
    implementation("io.ktor", "ktor-locations")
}

mikbotPlugin {
    description.set("Plugin requiring each invite of the bot to be manually confirmed by an owner")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }
}
