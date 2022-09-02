plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi.mikbot"
version = "2.3.0"

dependencies {
    compileOnly(projects.api)
}
