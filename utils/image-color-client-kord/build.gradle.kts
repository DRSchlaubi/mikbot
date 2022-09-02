plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.nycode"
version = "1.1.0"

dependencies {
    api(projects.utils.imageColorClient)
    api(libs.kord.core)
}

kotlin {
    explicitApi()
}
