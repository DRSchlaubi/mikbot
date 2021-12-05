package dev.schlaubi.mikbot.util_plugins.ktor.api

import io.ktor.application.*
import io.ktor.features.*
import org.pf4j.ExtensionPoint

/**
 * Ktor plugin extension point.
 */
interface KtorExtensionPoint : ExtensionPoint {
    /**
     * Customizes the Ktor application of the bot
     */
    fun Application.apply()

    /**
     * Customizes the Ktor's StatusPages feature
     */
    fun StatusPages.Configuration.apply() {}
}
