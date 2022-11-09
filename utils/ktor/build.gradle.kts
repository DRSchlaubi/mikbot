plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "2.8.0"

dependencies {
    // Verification Server
    api(libs.ktor.server.netty)
    api(libs.ktor.server.resources)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.server.html.builder)
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }
}
