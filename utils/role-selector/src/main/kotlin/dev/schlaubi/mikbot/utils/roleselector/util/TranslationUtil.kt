package dev.schlaubi.mikbot.utils.roleselector.util

import dev.schlaubi.mikbot.plugin.api.util.translateGlobally
import org.koin.core.component.KoinComponent

suspend fun KoinComponent.translateString(key: String, vararg arguments: Any?) =
    translateGlobally(
        key = key,
        bundleName = "roleselector",
        replacements = arguments as Array<Any?>
    )
