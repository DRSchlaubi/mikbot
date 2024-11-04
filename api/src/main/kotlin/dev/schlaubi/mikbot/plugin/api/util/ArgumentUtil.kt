package dev.schlaubi.mikbot.plugin.api.util

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.koin.KordExKoinComponent
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
