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
            freeCompilerArgs = experimentalAnnotations.map { "-opt-in=$it" }
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(20)
}

ktlint {
    disabledRules.add("no-wildcard-imports")
    filter {
        exclude { element ->
            val path = element.file.absolutePath
            path.contains("build") || path.contains("generated") || element.isDirectory
        }
    }
}
