plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "2.1.0"

dependencies {
    compileOnly(projects.api)
}
