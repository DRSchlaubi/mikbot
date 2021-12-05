import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val experimentalAnnotations =
    listOf("kotlin.RequiresOptIn", "kotlin.time.ExperimentalTime", "kotlin.contracts.ExperimentalContracts")

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = freeCompilerArgs + experimentalAnnotations.map { "-Xopt-in=$it" }
        }
    }
}

kotlin {
    jvmToolchain {
        (this as DefaultToolchainSpec).languageVersion.set(JavaLanguageVersion.of(16))
    }
}
