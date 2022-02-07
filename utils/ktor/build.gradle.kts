plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.0.4"

dependencies {
    // Verification Server
    api(platform("io.ktor:ktor-bom:1.6.7"))
    api("io.ktor", "ktor-server-netty")
    api("io.ktor", "ktor-locations")
    api("io.ktor", "ktor-serialization")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=dev.schlaubi.mikbot.plugin.api.InternalAPI")
        }
    }
}
