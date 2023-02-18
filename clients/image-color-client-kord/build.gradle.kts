plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.nycode"
version = "1.1.0"

dependencies {
    api(projects.clients.imageColorClient)
    api(libs.kord.rest)
}

kotlin {
    explicitApi()
}
