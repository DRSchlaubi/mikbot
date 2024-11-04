package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Command
import dev.kordex.core.commands.CommandContext
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import kotlinx.coroutines.runBlocking

/**
 * Accessor to [GuildBehavior] for contexts having the [anyGuild] check applied.
 */
public val CommandContext.safeGuild: GuildBehavior
    get() = runBlocking { getGuild() } ?: error("This command required a guild check")

/**
 * Accessor to [MemberBehavior] for contexts having the [anyGuild] check applied.
 */
public val CommandContext.safeMember: MemberBehavior
    get() = runBlocking { getMember() } ?: error("This command required a guild check")

/**
 * Adds [Command.kord] to [CommandContext] as implicit receivers are blocked.
 */
public inline val CommandContext.kord: Kord
    get() = command.kord
