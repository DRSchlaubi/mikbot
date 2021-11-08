plugins {
    `mikbot-module`
}

group = "dev.schlaubi"
version = "1.0.0"

dependencies {
    implementation("com.google.devtools.ksp", "symbol-processing-api", "1.5.31-1.0.0")
    implementation(project(":api:annotations"))
}
