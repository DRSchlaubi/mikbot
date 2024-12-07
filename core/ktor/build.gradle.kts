plugins {
    `mikbot-module`
    `mikbot-publishing`
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

group = "dev.schlaubi"
version = mikbotVersion

dependencies {
    // Verification Server
    api(libs.ktor.server.netty)
    api(libs.ktor.server.resources)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.cors)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.server.html.builder)
    api(libs.ktor.server.websockets)
    api(libs.kompendium.core)
    api(libs.kompendium.resources)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
    }
}
