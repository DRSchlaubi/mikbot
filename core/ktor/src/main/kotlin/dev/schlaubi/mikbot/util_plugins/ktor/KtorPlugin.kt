package dev.schlaubi.mikbot.util_plugins.ktor

import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.getExtensions
import dev.schlaubi.mikbot.util_plugins.ktor.api.Config
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.plus
import kotlin.coroutines.CoroutineContext

@PluginMain
class KtorPlugin(context: PluginContext) : Plugin(context), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val extensions = context.pluginSystem.getExtensions<KtorExtensionPoint>()

    private val json = Json {
        serializersModule = extensions.fold(EmptySerializersModule()) { prev, now ->
            prev + now.provideSerializersModule()
        }

        extensions.forEach {
            with(it) {
                apply()
            }
        }
    }

    @Suppress("ExtractKtorModule")
    override fun start() {
        launch {
            embeddedServer(Netty, port = Config.WEB_SERVER_PORT, host = Config.WEB_SERVER_HOST) {
                install(Resources)

                install(StatusPages) {
                    exception<NotFoundException> { call, _ ->
                        call.respond(HttpStatusCode.NotFound)
                    }
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
