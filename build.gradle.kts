import dev.schlaubi.mikbot.gradle.addRepositories

plugins {
    id("dev.schlaubi.mikbot.gradle-plugin")
}

subprojects {
    addRepositories()
}

mikbotPlugin {
    license.set("MIT License")
    provider.set("Mikbot Official Plugins")
}

pluginPublishing {
    repositoryUrl.set("https://storage.googleapis.com/mikbot-plugins")
    targetDirectory.set(rootProject.file("ci-repo").toPath())
    currentRepository.set(rootProject.file("ci-repo-old").toPath())
    projectUrl.set("https://github.com/DRSchlaubi/mikbot")
}
