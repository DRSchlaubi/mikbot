package dev.schlaubi.mikbot.util_plugins.ktor

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.*
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.util_plugins.ktor.api.Config
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.definition.MapDefinition
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.info.License
import io.bkbn.kompendium.oas.server.Server
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.plus
import java.net.URI
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.typeOf

private val baseSpec = OpenApiSpec(
    info = Info(
        title = "Mikbot API",
        version = MikBotInfo.VERSION,
        description = "API for interacting with Mikbot",
        license = License("MIT License", "MIT", URI("https://github.com/DRSchlaubi/mikbot/blob/main/LICENSE"))
    ),
    servers = mutableListOf(
        Server(Config.WEB_SERVER_URL.toURI())
    )
)

private val schmeHandler = KotlinxSerializationSchemaConfigurator()

private val baseTypes = mapOf(
    typeOf<Snowflake>() to TypeDefinition.STRING,
    typeOf<JsonObject>() to MapDefinition(TypeDefinition.STRING)
)

@PluginMain
class KtorPlugin(context: PluginContext) : Plugin(context), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    @Suppress("ExtractKtorModule")
    override fun start() {
        val extensions = contextSafe.pluginSystem.getExtensions<KtorExtensionPoint>()

        val json = Json {
            serializersModule = extensions.fold(EmptySerializersModule()) { prev, now ->
                prev + now.provideSerializersModule()
            }

            extensions.forEach {
                with(it) {
                    apply()
                }
            }
        }

        launch {
            @Suppress("ConvertLambdaToReference")
            embeddedServer(Netty, port = Config.WEB_SERVER_PORT, host = Config.WEB_SERVER_HOST) {
                install(NotarizedApplication()) {
                    spec = {
                        extensions.fold(baseSpec) { acc, it ->
                            with(it) {
                                acc.apply()
                            }
                        }
                    }
                    specRoute = { spec, routing ->
                        routing.route("/openapi.json") {
                            install(CORS) {
                                anyHost()
                                allowHeader(HttpHeaders.ContentType)
                            }
                            get {
                                call.respond(spec)
                            }
                        }
                    }
                    customTypes =
                        extensions.fold(baseTypes) { acc, it ->
                            acc + it.provideCustomTypes()
                        }
                    extensions.forEach {
                        with(it) {
                            apply()
                        }
                    }
                    schemaConfigurator = schmeHandler
                }
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

                    configureRedoc()
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
