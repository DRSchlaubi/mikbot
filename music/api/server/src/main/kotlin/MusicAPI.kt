package dev.schlaubi.mikmusic.api

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikmusic.api.authentication.authentication
import dev.schlaubi.mikmusic.api.authentication.withAuthentication
import dev.schlaubi.mikmusic.api.documentation.customType
import dev.schlaubi.mikmusic.api.player.*
import io.bkbn.kompendium.json.schema.definition.JsonSchema
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.security.BearerAuth
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.pf4j.Extension
import kotlin.reflect.KType

private val json = Json {
    ignoreUnknownKeys = true
}

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
}

private val LOG = KotlinLogging.logger { }

@PluginMain
class MusicAPI(context: PluginContext) : Plugin(context) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::StateWatcher)
    }

    override fun stop() {
        httpClient.close()
    }
}

@Extension
class MusicAPIServer : KtorExtensionPoint {
    override fun Application.apply() {
        authentication()

        if (pluginOrNull(WebSockets) == null) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }

        routing {
            musicApi()
        }
    }

    override fun StatusPagesConfig.apply() {
        installErrorHandler()
    }

    override fun provideCustomTypes(): Map<KType, JsonSchema> = customType

    override fun OpenApiSpec.apply(): OpenApiSpec = apply {
        components.securitySchemes["music_api_auth"] = BearerAuth("JWT")
    }
}

fun Routing.musicApi() {
    authentication()

    withAuthentication {
        searchRoute()
        playerRoute()
        queueRoute()
        channels()
    }
    webSocket()
}
