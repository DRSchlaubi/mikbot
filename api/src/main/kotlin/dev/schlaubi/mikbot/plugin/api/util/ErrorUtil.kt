package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.i18n.types.Key

/**
 * Throws a [DiscordRelayedException] with [message] and therefore sends it to the user.
 */
public fun discordError(message: Key): Nothing = throw DiscordRelayedException(message)
