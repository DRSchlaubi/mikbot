@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
import dev.schlaubi.mikbot.gradle.addRepositories
import java.util.Locale

plugins {
    dev.schlaubi.mikbot.`gradle-plugin`
}

subprojects {
    addRepositories()
    repositories {
        maven("https://maven.topi.wtf/releases")
    }
}

mikbotPlugin {
    license = "MIT License"
    provider = "Mikbot Official Plugins"

    i18n {
        classPackage = "dev.schlaubi.mikbot.translations"
    }
}

pluginPublishing {
    repositoryUrl = "https://storage.googleapis.com/mikbot-plugins"
    targetDirectory = rootProject.file("ci-repo")
    currentRepository = rootProject.file("ci-repo-old")
    projectUrl = "https://github.com/DRSchlaubi/mikbot"
}

tasks {
    task("buildDockerImage") {
        dependsOn(":runtime:installDist")
    }
}
