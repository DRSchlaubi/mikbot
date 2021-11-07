plugins {
    `mikbot-plugin`
    `mikbot-module`
}

group = "dev.schlaubi.mikbot"
version = "1.0-SNAPSHOT"

dependencies {
    plugin(project(":game:game-api"))
    implementation(project(":game:uno"))
}
