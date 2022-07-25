import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.*

plugins {
    id("com.google.devtools.ksp") // used for plugin-processor
    kotlin("jvm")
    id("dev.schlaubi.mikbot.gradle-plugin")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8")) // this one is included in the bot itself
    compileOnly(project(":api"))
    ksp(project(":plugin-processor"))
}

tasks {
    val generateDefaultResourceBundle = task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
        defaultLocale.set(Locale("en", "GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }
}

mikbotPlugin {
    license.set("MIT License")
    provider.set("Mikbot Official Plugins")
}

pluginPublishing {
    enabled.set(true)
    repositoryUrl.set("https://storage.googleapis.com/mikbot-plugins")
    targetDirectory.set(rootProject.file("ci-repo").toPath())
    currentRepository.set(rootProject.file("ci-repo-old").toPath())
    projectUrl.set("https://github.com/DRSchlaubi/tree/main/${project.path.drop(1).replace(":", "/")}")
}
