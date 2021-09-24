package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.schlaubi.musicbot.config.Config

fun SlashCommand<*, *>.ownerOnly() {
    allowByDefault = false
    Config.BOT_OWNERS.forEach(::allowUser)
}
