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
    jvmToolchain(25)

    compilerOptions {
        freeCompilerArgs.addAll(experimentalAnnotations.map { "-opt-in=$it" })
    }
}
