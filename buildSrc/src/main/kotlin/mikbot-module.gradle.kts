import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

val experimentalAnnotations =
    listOf("kotlin.RequiresOptIn", "kotlin.time.ExperimentalTime", "kotlin.contracts.ExperimentalContracts")

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + experimentalAnnotations.map { "-Xopt-in=$it" }
        }
    }
}

kotlin {
    jvmToolchain {
        (this as DefaultToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

ktlint {
    disabledRules.add("no-wildcard-imports")
}
