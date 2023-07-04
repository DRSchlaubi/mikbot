import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `mikbot-module`
    alias(libs.plugins.kotlinx.serialization)
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
}

dependencies {
    plugin(projects.music.player)
    optionalPlugin(projects.core.gdpr)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

}

mikbotPlugin {
    description = "Plugin providing full music related commands"
}
