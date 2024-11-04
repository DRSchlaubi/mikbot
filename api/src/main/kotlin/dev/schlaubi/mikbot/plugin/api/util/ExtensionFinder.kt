package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.extensions.Extension

/**
 * Allows to lazily access other extensions in an [Extension].
 *
 * Example:
 * ```kotlin
 * val musicModule: MusicModule by extension()
 * ```
 */
public inline fun <reified T> Extension.extension(): Lazy<T> = lazy { bot.findExtension<T>()!! }
