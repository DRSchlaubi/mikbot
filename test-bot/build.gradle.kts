plugins {
    `mikbot-module`
    `mikbot-bot`
}

group = "dev.schlaubi"
version = "2.1.0"

dependencies {
    implementation(project(":"))
    implementation(projects.api)
}
