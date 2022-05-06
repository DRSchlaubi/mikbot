package dev.schlaubi.mikbot.gradle

import org.gradle.api.Project

// great name i know
fun Project.extractMikBotVersionFromProjectApiDependency(): String? {
    val mikbotConfiguration = project.configurations.getByName("mikbot")
    val dependency = mikbotConfiguration.resolvedConfiguration.firstLevelModuleDependencies.singleOrNull()
        ?: error("Specify the mikbot-api dependency as mikbot(\"dev.schlaubi\", \"mikbot-api\", \"x.x.x\"). DO NOT SPECIFY OTHER DEPENDENCIES WITH THE MIKBOT CONFIGURATION.")
    return dependency.moduleVersion
}
