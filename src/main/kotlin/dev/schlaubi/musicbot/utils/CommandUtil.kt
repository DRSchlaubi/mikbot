package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import dev.kord.core.behavior.GuildBehavior

val SlashCommandContext<*, *>.safeGuild: GuildBehavior
    get() = guild ?: error("This command required a guild check")
