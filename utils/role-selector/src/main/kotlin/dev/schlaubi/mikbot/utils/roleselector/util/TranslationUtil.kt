package dev.schlaubi.mikbot.utils.roleselector.util

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.util.translateGlobally

@Suppress("UNCHECKED_CAST")
suspend fun KordExKoinComponent.translateString(key: String, vararg arguments: Any?) =
    translateGlobally(
        key = key,
        bundleName = "roleselector",
        replacements = arguments as Array<Any?>
    )

internal fun ApplicationCommand<*>.setTranslationKey() {
    bundle = "roleselector"
}
