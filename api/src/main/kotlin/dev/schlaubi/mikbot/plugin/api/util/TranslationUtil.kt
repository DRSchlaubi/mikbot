package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.types.TranslatableContext

/**
 * Translates [key] with [replacements].
 */
public suspend fun TranslatableContext.translate(key: Key, vararg replacements: Any?): String =
    key.withContext(this).translate(*replacements)
