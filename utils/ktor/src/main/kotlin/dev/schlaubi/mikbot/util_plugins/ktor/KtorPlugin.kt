package dev.schlaubi.mikbot.util_plugins.ktor

import dev.schlaubi.mikbot.plugin.api.*
import dev.schlaubi.mikbot.util_plugins.ktor.api.Config
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.plus
import kotlin.coroutines.CoroutineContext

private val extensions = pluginSystem.getExtensions<KtorExtensionPoint>()

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    serializersModule = extensions.fold(EmptySerializersModule) { prev, now ->
        prev + now.provideSerializersModule()
    }

    extensions.forEach {
        with(it) {
            apply()
        }
    }
}

@PluginMain
class KtorPlugin(wrapper: PluginWrapper) : Plugin(wrapper), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    override fun start() {
        launch {
            embeddedServer(Netty, port = Config.WEB_SERVER_PORT, host = Config.WEB_SERVER_HOST) {
                install(Locations)

                install(StatusPages) {
                    exception<NotFoundException> { call.respond(HttpStatusCode.NotFound) }
                    extensions.forEach {
                        with(it) {
                            apply()
                        }
                    }
                }

                install(ContentNegotiation) {
                    json(json)
                }

                routing {
                    handle {
                        call.respondRedirect("https://teamseas.org")
                    }
                }

                extensions.forEach {
                    with(it) {
                        apply()
                    }
                }
            }.start()
        }
    }

    override fun stop() {
        cancel()
    }
}
