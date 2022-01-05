package dev.schlaubi.mikbot.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import java.nio.file.Path

internal const val pluginExtensionName = "mikbotPlugin"

// This is just there for usage in the Plugin, users of the plugin should use Gradle's generated accessors
internal val ExtensionAware.mikbotPluginExtension: PluginExtension
    get() = extensions.findByName(pluginExtensionName) as PluginExtension

/**
 * Extension for configuring plugins for PF4J.
 */
abstract class PluginExtension {
    /**
     * Plugin id used for building and publishing (defaults to the project name)
     */
    abstract val pluginId: Property<String>

    /**
     * The version of the application this plugin requires (optional).
     */
    abstract val requires: Property<String?>

    /**
     * The description of the plugin.
     */
    abstract val description: Property<String>

    /**
     * The author of the plugin.
     */
    abstract val provider: Property<String>

    /**
     * The license the plugin is licensed under.
     */
    abstract val license: Property<String>

    /**
     * Whether to ignore mikbot dependencies.
     * **TL;DR** If you make a Mikbot plugin leave this turned on, if not turn it off
     *
     * This disables the plugin automatic dependency filtering for Mikbot,
     * some transitive dependencies of plugins are shared with mikbot, but not detected as duplicated by
     * Gradle because of version conflicts, including them will result in a runtime class loading error,
     * therefore this plugin removes any duplicates with Mikbot from the output of the `assemblePlugin`
     * task to avoid these issues.
     * However, if you don't make a mikbot plugin this doesn't make sense to do, so you should disable this settings
     */
    abstract val ignoreDependencies: Property<Boolean>

    /**
     * The location of the plugins main file.
     *
     * If you use the KSP processor you don't need to worry about this.
     */
    abstract val pluginMainFileLocation: Property<Path>

}

internal val Project.pluginId: String
    get() = mikbotPluginExtension.pluginId.getOrElse(name)
