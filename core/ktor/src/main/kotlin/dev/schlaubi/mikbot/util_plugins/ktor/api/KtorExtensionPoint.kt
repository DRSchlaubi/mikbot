package dev.schlaubi.mikbot.util_plugins.ktor.api

import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.definition.JsonSchema
import io.bkbn.kompendium.oas.OpenApiSpec
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.pf4j.ExtensionPoint
import kotlin.reflect.KType

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
    fun provideSerializersModule(): SerializersModule = EmptySerializersModule()

    /**
     * Add extension specific [JsonBuilder] options.
     */
    fun JsonBuilder.apply() {}

    /**
     * Provides [NotarizedApplication.Config.customTypes] for this extension.
     */
    fun provideCustomTypes(): Map<KType, JsonSchema> = emptyMap()

    /**
     * Provides [NotarizedApplication] configuration for this extension.
     */
    fun NotarizedApplication.Config.apply() {}

    /**
     * Configures the base [OpenApiSpec].
     */
    fun OpenApiSpec.apply(): OpenApiSpec = this
}
