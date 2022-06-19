package dev.schlaubi.mikbot.plugin.api.util

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.io.Database
import org.koin.core.component.KoinComponent

/**
 * Simple accessor for [ExtensibleBot] for all [KoinComponents][KoinComponent]
 */
public val CommandContext.bot: ExtensibleBot
    get() = getKoin().get()

/**
 * Simple accessor for [Database] for all [KoinComponents][KoinComponent]
 */
public val KordExKoinComponent.database: Database
    get() = getKoin().get()
