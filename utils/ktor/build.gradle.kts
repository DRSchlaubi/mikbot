plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.0.5"

dependencies {
    // Verification Server
    api(libs.ktor.server.netty)
    api(libs.ktor.locations)
    api(libs.ktor.serialization)
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }
}
