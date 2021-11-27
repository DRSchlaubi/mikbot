package dev.schlaubi.mikbot.gradle

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

internal val pluginExtensionName = "mikbotPlugin"

val ExtensionAware.mikbotPluginExtension: PluginExtension
    get() = extensions.findByName(pluginExtensionName) as PluginExtension

abstract class PluginExtension {
    abstract val requires: Property<String>
    abstract val description: Property<String>
    abstract val provider: Property<String>
    abstract val license: Property<String>
}
