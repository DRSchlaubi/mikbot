plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "1.0.0"

dependencies {
    compileOnly(projects.api)
}
