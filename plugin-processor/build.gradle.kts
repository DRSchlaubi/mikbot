plugins {
    `mikbot-module`
    `mikbot-publishing`
}

group = "dev.schlaubi"
version = "2.3.0"

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.api.annotations)
    implementation(libs.pf4j)
}


tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
