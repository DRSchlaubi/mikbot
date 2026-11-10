package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.i18n.withContext
import dev.kordex.core.types.TranslatableContext
import dev.kordex.i18n.Key

/**
 * Translates [key] with [replacements].
 */
public suspend fun TranslatableContext.translate(key: Key, vararg replacements: Any?): String =
    key.withContext(this).translate(*replacements)
