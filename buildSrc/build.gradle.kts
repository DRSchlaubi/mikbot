plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.5.31"))
    implementation("com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin", "1.5.31-1.0.0")
    implementation(gradleApi())
    implementation(localGroovy())
}
