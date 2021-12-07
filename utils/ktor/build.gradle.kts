plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.0.1"

dependencies {
    // Verification Server
    api(platform("io.ktor:ktor-bom:1.6.2"))
    api("io.ktor", "ktor-server-netty")
    api("io.ktor", "ktor-locations")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }
}
