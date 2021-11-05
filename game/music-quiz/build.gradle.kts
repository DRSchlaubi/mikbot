plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(project(":game:game-api"))
    compileOnly(project(":music"))
}
