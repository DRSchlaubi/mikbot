plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = mikbotVersion

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.api.annotations)
    implementation(libs.pf4j)
}
