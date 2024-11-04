package dev.schlaubi.musicbot.core.plugin

import dev.kordex.core.i18n.ResourceBundleTranslations
import dev.kordex.core.i18n.TranslationsProvider
import dev.kord.common.asJavaLocale
import dev.kord.common.kLocale
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import mu.KotlinLogging
import java.util.*

private val LOG = KotlinLogging.logger { }

/**
 * Implementation of [TranslationsProvider] handling different plugin class loaders.
 */
class PluginTranslationProvider(private val pluginLoader: PluginLoader, defaultLocaleBuilder: () -> Locale) : ResourceBundleTranslations(defaultLocaleBuilder) {
    override fun getResourceBundle(bundle: String, locale: Locale, control: ResourceBundle.Control): ResourceBundle {
        val plugin = pluginLoader.getPluginForBundle(bundle)
        val classLoader =
            plugin?.pluginClassLoader ?: ClassLoader.getSystemClassLoader()
        LOG.debug { "Found classloader for $bundle to be $classLoader (${plugin?.pluginId ?: "<root>"})" }

        return ResourceBundle.getBundle(bundle, locale.kLocale.convertToISO().asJavaLocale(), classLoader, control)
    }
}
