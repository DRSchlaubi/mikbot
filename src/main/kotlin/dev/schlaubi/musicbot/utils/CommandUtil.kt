package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.core.behavior.GuildBehavior
import kotlinx.coroutines.runBlocking

/**
 * Accessor to [GuildBehavior] for contexts having the [anyGuild] check applied.
 */
val CommandContext.safeGuild: GuildBehavior
    get() = runBlocking { getGuild() } ?: error("This command required a guild check")
