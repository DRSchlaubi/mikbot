plugins {
    `mikbot-plugin`
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.0.0"

dependencies {
    // Verification Server
    api(platform("io.ktor:ktor-bom:1.6.2"))
    api("io.ktor", "ktor-server-netty")
    api("io.ktor", "ktor-locations")
}
