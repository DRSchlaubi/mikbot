package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.events.EventContext
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.pluginSystem

/**
 * Translates this  [key] from [bundleName] with [replacements].
 *
 * Only call this on [EventContext], [ExtensibleBot] or [CommandContext]
 */
// Oh, for god's sake, why is this the one time I want union types
public suspend fun KordExKoinComponent.translateGlobally(key: String, bundleName: String, replacements: Array<Any?>): String {
    return when (this) {
        is CommandContext -> {
            this.translate(key, bundleName, replacements)
        }
        is EventContext<*> -> this.translate(key, bundleName, replacements)
        is ExtensibleBot -> pluginSystem.translate(key, bundleName, replacements = replacements)
        else -> error("Please only call this on Instances of ExtensibleBot, CommandContext and EventContext<*>")
    }
}
