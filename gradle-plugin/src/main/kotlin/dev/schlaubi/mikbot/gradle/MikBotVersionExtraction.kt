package dev.schlaubi.mikbot.gradle

import org.gradle.api.Project

// great name i know
fun Project.extractMikBotVersionFromProjectApiDependency(): String = MikBotPluginInfo.VERSION
