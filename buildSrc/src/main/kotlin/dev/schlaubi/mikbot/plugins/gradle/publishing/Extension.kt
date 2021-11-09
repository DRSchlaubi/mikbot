package dev.schlaubi.mikbot.plugins.gradle.publishing

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

val pluginExtensionName = "mikbotPlugin"

val ExtensionAware.mikbotPluginExtension: PluginExtension
    get() = extensions.findByName(pluginExtensionName) as PluginExtension

abstract class PluginExtension {
    abstract val mainClass: Property<String>
    abstract val requires: Property<String>
    abstract val dependencies: ListProperty<String>
    abstract val description: Property<String>
    abstract val provider: Property<String>
    abstract val license: Property<String>
}
