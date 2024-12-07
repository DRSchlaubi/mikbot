package dev.schlaubi.mikmusic.api.player

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.common.entity.optional.value
import dev.kord.core.Kord
import dev.kord.core.event.guild.VoiceServerUpdateEvent
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.koin.KordExContext
import dev.kordex.core.utils.waitFor
import dev.schlaubi.lavakord.UnsafeRestApi
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.kord.updatePlayer
import dev.schlaubi.lavakord.rest.decodeTrack
import dev.schlaubi.mikmusic.api.authentication.authenticatedUser
import dev.schlaubi.mikmusic.api.documentation.documentedDelete
import dev.schlaubi.mikmusic.api.documentation.documentedGet
import dev.schlaubi.mikmusic.api.documentation.documentedPatch
import dev.schlaubi.mikmusic.api.requirePermission
import dev.schlaubi.mikmusic.api.toPlayerState
import dev.schlaubi.mikmusic.api.toSchedulingOptions
import dev.schlaubi.mikmusic.api.toSelectedPlayer
import dev.schlaubi.mikmusic.api.types.Routes
import dev.schlaubi.mikmusic.api.types.SimpleQueuedTrack
import dev.schlaubi.mikmusic.api.types.UpdatablePlayerState
import dev.schlaubi.mikmusic.api.types.UpdatablePlayerState.SchedulerSettings
import dev.schlaubi.mikmusic.core.MusicModule
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds

val kord by KordExContext.get().inject<Kord>()
val extensibleBoolean by KordExContext.get().inject<ExtensibleBot>()
val musicExtension by lazy { extensibleBoolean.findExtension<MusicModule>()!! }

fun Route.playerRoute() {
    getPlayers()
    getPlayer()
    patchPlayer()
    deletePlayer()
}

private fun Route.getPlayers() = documentedGet<Routes.Players> {
    val userGuilds = call.fetchGuilds().mapNotNull {
        val guild = kord.getGuildOrNull(it.id) ?: return@mapNotNull null // skip guilds the bot is not on
        val player = musicExtension.getMusicPlayer(guild)

        player.toPlayerState(guild)
    }

    call.respond(userGuilds.toList())
}

private fun Route.getPlayer() = documentedGet<Routes.Players.Specific> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    val player = musicExtension.getMusicPlayer(guild)

    call.respond(player.toSelectedPlayer(userId))
}

@OptIn(UnsafeRestApi::class)
private fun Route.patchPlayer() = documentedPatch<Routes.Players.Specific> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    val player = musicExtension.getMusicPlayer(guild)
    player.toSelectedPlayer(userId).requirePermission()

    val update = call.receive<UpdatablePlayerState>()

    if (update.channel !is Optional.Missing) {
        val channel = update.channel.value
        if (channel == null) {
            player.disconnectAudio()
        } else {
            player.connectAudio(update.channel.value!!)
            // Wait for bot to connect
            kord.waitFor<VoiceServerUpdateEvent>(10.seconds) { guildId == guild.id }
        }
    }

    player.node.updatePlayer(guild.id, request = update.toLavalinkUpdate())
    if (update.track is Optional.Value) {
        val track = SimpleQueuedTrack(player.node.decodeTrack(update.track.value!!), userId)
        player.queueTrack(
            force = true,
            onTop = false,
            tracks = listOf(track),
            position = update.position.value,
            schedulingOptions = update.schedulerSettings.value?.toSchedulingOptions()
        )
    }

    if (update.schedulerSettings is Optional.Value<SchedulerSettings>) {
        val schedulerSettings = update.schedulerSettings.value!!

        if (schedulerSettings.loopQueue is OptionalBoolean.Value) {
            player.loopQueue = schedulerSettings.loopQueue.value!!
        }
        if (schedulerSettings.repeat is OptionalBoolean.Value) {
            player.repeat = schedulerSettings.repeat.value!!
        }
        if (schedulerSettings.shuffle is OptionalBoolean.Value) {
            player.shuffle = schedulerSettings.shuffle.value!!
        }
        player.updateMusicChannelMessage()
    }

    call.respond(HttpStatusCode.Accepted)
}

private fun Route.deletePlayer() = documentedDelete<Routes.Players.Specific> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    val player = musicExtension.getMusicPlayer(guild)
    player.toSelectedPlayer(userId).requirePermission()

    player.stop()
    call.respond(HttpStatusCode.Accepted)
}
