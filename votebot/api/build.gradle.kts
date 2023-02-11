import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `mikbot-module`
    `mikbot-plugin`
    kotlin("plugin.serialization")
}

dependencies {
    plugin(projects.votebot.plugin)
    plugin(projects.utils.ktor)
    implementation(libs.ktor.server.cors) {
        exclude("io.ktor", "ktor-server-core")
    }
    implementation(libs.ktor.server.auth) {
        exclude("io.ktor", "ktor-server-core")
    }

    implementation(libs.bundles.jjwt)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
        }
    }
}
