package dev.schlaubi.mikbot.utils.roleselector.util

import dev.schlaubi.mikbot.plugin.api.util.translateGlobally
import org.koin.core.component.KoinComponent

@Suppress("UNCHECKED_CAST")
suspend fun KoinComponent.translateString(key: String, vararg arguments: Any?) =
    translateGlobally(
        key = key,
        bundleName = "roleselector",
        replacements = arguments as Array<Any?>
    )
