plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.nycode"
version = mikbotVersion

dependencies {
    api(projects.clients.imageColorClient)
    api(libs.kord.rest)
}

kotlin {
    explicitApi()
}
