import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.*

plugins {
    `mikbot-module`
    com.google.devtools.ksp
    dev.schlaubi.mikbot.`gradle-plugin`
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    plugin(projects.music.player)
    plugin(projects.core.ktor)
    ktorDependency(libs.ktor.server.cors)
}

mikbotPlugin {
    pluginId = "music-lyrics"
    description = "Plugin providing lyrics for the music plugin"
}

fun DependencyHandlerScope.ktorDependency(dependency: ProviderConvertible<*>) = ktorDependency(dependency.asProvider())
fun DependencyHandlerScope.ktorDependency(dependency: Provider<*>) = implementation(dependency) {
    exclude(module = "ktor-server-core")
}

tasks {
    val generateDefaultResourceBundle by registering(GenerateDefaultTranslationBundleTask::class) {
        defaultLocale = Locale("en", "GB")
    }

    classes {
        dependsOn(generateDefaultResourceBundle)
    }
}
