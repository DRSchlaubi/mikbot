package dev.schlaubi.mikbot.gradle.extension

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * @see dev.kordex.gradle.plugins.i18n.I18nExtension
 */
abstract class MikBotI18NSettings {

    abstract val classPackage: Property<String>
    abstract val translationBundle: Property<String>

    abstract val className: Property<String>

    abstract val configureSourceSet: Property<Boolean>

    abstract val outputDirectory: DirectoryProperty
    abstract val publicVisibility: Property<Boolean>
}
