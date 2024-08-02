package dev.schlaubi.mikbot.gradle.extension

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.util.*

/** Extension for configuring plugins for PF4J. */
abstract class PluginExtension {
    /** Plugin id used for building and publishing (defaults to the project name) */
    abstract val pluginId: Property<String>

    /** The version of the application this plugin requires (optional). */
    abstract val requires: Property<String?>

    /** The description of the plugin. */
    abstract val description: Property<String>

    /** The author of the plugin. */
    abstract val provider: Property<String>

    /** The license the plugin is licensed under. */
    abstract val license: Property<String>

    /**
     * Whether to ignore mikbot dependencies. **TL;DR** If you make a Mikbot plugin leave this
     * turned on, if not turn it off
     *
     * This disables the plugin automatic dependency filtering for Mikbot, some transitive
     * dependencies of plugins are shared with mikbot, but not detected as duplicated by Gradle
     * because of version conflicts, including them will result in a runtime class loading error,
     * therefore this plugin removes any duplicates with Mikbot from the output of the
     * `assemblePlugin` task to avoid these issues. However, if you don't make a mikbot plugin this
     * doesn't make sense to do, so you should disable this settings
     */
    abstract val ignoreDependencies: Property<Boolean>

    /**
     * The location of the plugins main file.
     *
     * If you use the KSP processor you don't need to worry about this.
     */
    abstract val pluginMainFileLocation: RegularFileProperty

    /**
     * The optional override for the projects resources bundle folder.
     */
    abstract val bundle: Property<String>

    /**
     * Whether to apply the processor to process KordEx converter processors:
     * See documentation [here](https://github.com/Kord-Extensions/kord-extensions/tree/root/annotations/src/main/kotlin/com/kotlindiscord/kord/extensions/modules/annotations/converters)
     */
    abstract val enableKordexProcessor: Property<Boolean>

    /**
     * The default locale.
     */
    abstract val defaultLocale: Property<Locale>

    /**
     * List of repositories used to download plugins bundled with the bot distribution.
     */
    abstract val repositories: ListProperty<String>
}

internal val Project.pluginId: String
    get() = mikbotPluginExtension.pluginId.getOrElse(name)

internal val Project.bundle: String
    get() = mikbotPluginExtension.bundle.getOrElse(name)
