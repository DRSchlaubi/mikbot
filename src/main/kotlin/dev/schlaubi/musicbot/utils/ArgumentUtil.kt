package dev.schlaubi.musicbot.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.schlaubi.musicbot.core.io.Database
import org.koin.core.component.KoinComponent

/**
 * Simple accessor for [ExtensibleBot] for all [KoinComponents][KoinComponent]
 */
val CommandContext.bot: ExtensibleBot
    get() = getKoin().get()

/**
 * Simple accessor for [Database] for all [KoinComponents][KoinComponent]
 */
val KoinComponent.database: Database
    get() = getKoin().get()
