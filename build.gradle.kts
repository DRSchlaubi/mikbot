@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
import dev.schlaubi.mikbot.gradle.addRepositories

plugins {
    dev.schlaubi.mikbot.`gradle-plugin`
}

subprojects {
    addRepositories()
    repositories {
        mavenLocal()
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
