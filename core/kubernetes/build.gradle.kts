plugins {
    `mikbot-module`
    `mikbot-publishing`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
    `jvm-test-suite`
}

group = "dev.schlaubi.mikbot"
version = mikbotVersion

repositories {
    maven("https://jitpack.io")
}

dependencies {
    plugin(projects.core.ktor)

    implementation(libs.kubernetes.client)
    implementation(libs.kotlin.jsonpatch)
    implementation(libs.bucket4j)


    testImplementation(kotlin("test-junit5"))
    testImplementation(projects.api)
    testImplementation(libs.kord.core)
}

testing {
    suites {
        @Suppress("UnstableApiUsage")
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }
    }
}

mikbotPlugin {
    description = "Plugin providing an /healthz endpoint used for health checking."
}
