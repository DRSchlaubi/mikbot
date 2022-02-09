plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "1.1.0"

dependencies {
    implementation("com.google.devtools.ksp", "symbol-processing-api", "1.6.10-1.0.2")
    implementation(project(":api:annotations"))
    implementation("org.pf4j", "pf4j", "3.6.0")
}
