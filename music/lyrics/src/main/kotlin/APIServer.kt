package dev.schlaubi.mikmusic.lyrics

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.cache.api.query
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.cache.data.VoiceStateData
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.plugins.lyrics.rest.requestLyrics
import dev.schlaubi.lyrics.protocol.TimedLyrics
import dev.schlaubi.mikbot.plugin.api.config.Environment
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.lyrics.events.Event
import dev.schlaubi.mikmusic.lyrics.events.NextTrackEvent
import dev.schlaubi.mikmusic.lyrics.events.PlayerStateUpdateEvent
import dev.schlaubi.mikmusic.lyrics.events.PlayerStoppedEvent
import dev.schlaubi.mikmusic.player.MusicPlayer
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import org.pf4j.Extension
import kotlin.collections.set
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.mikbot.plugin.api.config.Config as BotConfig

private val PLAYER = AttributeKey<MusicPlayer>("MUSIC_PLAYER")
private val authKeys = mutableMapOf<String, Snowflake>()

fun requestToken(userId: Snowflake): String {
    val key = generateNonce()
    authKeys[key] = userId
    return key
}

@Extension
class APIServer : KtorExtensionPoint, KordExKoinComponent {

    private val bot by inject<ExtensibleBot>()
    private val musicModule by lazy { bot.findExtension<MusicModule>()!! }

    private val ApplicationCall.userId: Snowflake
        get() {
            val header = request.authorization() ?: parameters["api_key"] ?: unauthorized()
            return authKeys[header] ?: unauthorized()
        }

    @OptIn(KordUnsafe::class, KordExperimental::class)
    private suspend fun Snowflake.findPlayer(): MusicPlayer {
        val voiceState = bot.kordRef.findVoiceState(this) ?: notFound()
        val player = musicModule.getMusicPlayer(bot.kordRef.unsafe.guild(voiceState.guildId))

        return player.takeIf { it.playingTrack != null } ?: notFound()
    }

    override fun Application.apply() {
        if (pluginOrNull(WebSockets) == null) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowHeader(HttpHeaders.Authorization)
            if (BotConfig.ENVIRONMENT == Environment.DEVELOPMENT) {
                anyHost()
            } else {
                allowSameOrigin = true
                val url = Url(Config.LYRICS_WEB_URL)
                allowHost(url.host, listOf(url.protocol.name))
            }
        }

        routing {
            route("lyrics") {
                get("current") {
                    val player = call.userId.findPlayer()

                    call.respond(player.player.requestLyrics().takeIf { it is TimedLyrics } ?: notFound())
                }

                route("events") {
                    intercept(ApplicationCallPipeline.Plugins) {
                        call.attributes.put(PLAYER, call.userId.findPlayer())
                        proceed()
                    }

                    webSocket {
                        val player = call.attributes[PLAYER].player
                        val listenerScope = this
                        launch {
                            var state = player.toState()
                            while (isActive) {
                                val newState = player.toState()
                                if (newState != state && state.playing) {
                                    state = newState
                                    sendSerialized<Event>(newState)
                                }

                                delay(1.seconds)
                            }
                        }

                        player.on<TrackEndEvent>(listenerScope) {
                            if (!reason.mayStartNext) {
                                sendSerialized<Event>(PlayerStoppedEvent)
                            }
                        }
                        player.on<TrackStartEvent>(listenerScope) {
                            sendSerialized<Event>(NextTrackEvent(player.position))
                        }
                        bot.kordRef.on<VoiceStateUpdateEvent>(listenerScope) {
                            if (state.userId == call.userId && state.channelId == null && old?.channelId != null) {
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Left voice channel"))
                            }
                        }

                        // Wait for connection to die or websocket getting closed otherwise
                        awaitCancellation()
                    }
                }
            }
        }
    }

    override fun StatusPagesConfig.apply() {
        exception<UnauthorizedException> { call, _ ->
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}

suspend fun Kord.findVoiceState(userId: Snowflake): VoiceStateData? {
    return cache.query<VoiceStateData> {
        VoiceStateData::channelId ne null
        VoiceStateData::userId eq userId
    }.singleOrNull()
}

private class UnauthorizedException : RuntimeException()

private fun unauthorized(): Nothing = throw UnauthorizedException()
private fun notFound(): Nothing = throw NotFoundException()

private fun Player.toState() = PlayerStateUpdateEvent(!paused, position, Clock.System.now())
