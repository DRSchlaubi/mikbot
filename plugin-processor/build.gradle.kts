plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.0.0"

dependencies {
    implementation("com.google.devtools.ksp", "symbol-processing-api", "1.5.31-1.0.0")
    implementation(project(":api:annotations"))
    implementation("org.pf4j", "pf4j", "3.6.0")
}
