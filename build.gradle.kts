@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
import dev.schlaubi.mikbot.gradle.addRepositories

plugins {
    id("dev.schlaubi.mikbot.gradle-plugin")
}

subprojects {
    addRepositories()
}

mikbotPlugin {
    license = "MIT License"
    provider = "Mikbot Official Plugins"
}

pluginPublishing {
    repositoryUrl = "https://storage.googleapis.com/mikbot-plugins"
    targetDirectory = rootProject.file("ci-repo").toPath()
    currentRepository = rootProject.file("ci-repo-old").toPath()
    projectUrl = "https://github.com/DRSchlaubi/mikbot"
}

tasks {
    task("buildDockerImage") {
        dependsOn(":runtime:installDist")
    }
}
