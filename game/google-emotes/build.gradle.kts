plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "2.0.0"

dependencies {
    compileOnly(projects.api)
}
