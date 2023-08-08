import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION") plugins {
    alias(libs.plugins.gradle.publish)
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

group = "dev.schlaubi"
version = libs.versions.api.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.jdk8)
    compileOnly(kotlin("gradle-plugin"))
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("mikbot-plugin-gradle-plugin") {
            id = "dev.schlaubi.mikbot.gradle-plugin"
            implementationClass = "dev.schlaubi.mikbot.gradle.MikBotPluginGradlePlugin"
            displayName = "Mikbot Gradle Plugin"
            description = "Utility plugin to build Mikbot and PF4J plugins"
            tags = setOf("mikbot", "pf4j", "plugins", "kotlin")
        }
    }

    website = "https://github.com/DRSchlaubi/mikbot/tree/main/gradle-plugin"
    vcsUrl = "https://github.com/DRSchlaubi/mikbot"

}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }
}

afterEvaluate {
    buildConfig {
        packageName("dev.schlaubi.mikbot.gradle")
        className("MikBotPluginInfo")
        buildConfigField("String", "VERSION", "\"${libs.versions.api.get()}\"")
        buildConfigField("String", "KORDEX_VERSION", "\"${libs.versions.kordex.get()}\"")
        buildConfigField("boolean", "IS_MIKBOT", (System.getenv("BUILD_PLUGIN_CI")?.toBoolean() != true).toString())
    }
}
