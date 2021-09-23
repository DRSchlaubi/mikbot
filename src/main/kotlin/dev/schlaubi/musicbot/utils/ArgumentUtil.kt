package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.schlaubi.musicbot.core.io.Database

val CommandContext.bot: ExtensibleBot
    get() = getKoin().get()

val CommandContext.database: Database
    get() = getKoin().get()
