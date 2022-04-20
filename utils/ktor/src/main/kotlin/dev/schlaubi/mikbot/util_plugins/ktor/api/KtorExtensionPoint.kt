package dev.schlaubi.mikbot.util_plugins.ktor.api

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
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
    fun StatusPagesConfig.apply() {}

    /**
     * Provides the serializers module for this extenion.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun provideSerializersModule(): SerializersModule = EmptySerializersModule

    /**
     * Add extension specific [JsonBuilder] options.
     */
    fun JsonBuilder.apply() {}
}
