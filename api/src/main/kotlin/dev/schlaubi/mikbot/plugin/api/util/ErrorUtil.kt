package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.DiscordRelayedException

/**
 * Throws a [DiscordRelayedException] with [message] and therefore sends it to the user.
 */
public fun discordError(message: String): Nothing = throw DiscordRelayedException(message)
