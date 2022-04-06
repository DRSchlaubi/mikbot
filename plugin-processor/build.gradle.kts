plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "2.0.0"

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.api.annotations)
    implementation(libs.pf4j)
}
