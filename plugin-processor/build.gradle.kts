plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.1.1"

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.api.annotations)
    implementation(libs.pf4j)
}
