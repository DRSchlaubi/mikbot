package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.schlaubi.musicbot.config.Config

fun SlashCommand<*, *>.ownerOnly() {
    allowByDefault = false
    if (Config.OWNER_GUILD != null) {
        guildId = Config.OWNER_GUILD!!
    }
    Config.BOT_OWNERS.forEach(::allowUser)
}
