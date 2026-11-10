import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

val experimentalAnnotations =
    listOf("kotlin.RequiresOptIn", "kotlin.time.ExperimentalTime", "kotlin.contracts.ExperimentalContracts")

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(27)

    compilerOptions {
        jvmTarget = JvmTarget.JVM_26
        freeCompilerArgs.addAll(experimentalAnnotations.map { "-opt-in=$it" })
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_26
}
