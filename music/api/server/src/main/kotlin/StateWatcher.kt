package dev.schlaubi.mikmusic.api

import dev.kord.cache.api.query
import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.VoiceStateData
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.schlaubi.lavakord.audio.Event
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.mikbot.plugin.api.util.extension
import dev.schlaubi.mikmusic.api.player.broadcastEvent
import dev.schlaubi.mikmusic.api.player.getUsersInChannel
import dev.schlaubi.mikmusic.api.player.sendEventToUser
import dev.schlaubi.mikmusic.api.types.Player
import dev.schlaubi.mikmusic.api.types.PlayerUpdateEvent
import dev.schlaubi.mikmusic.api.types.QueueUpdateEvent
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.mikmusic.player.Queue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import dev.schlaubi.lavakord.audio.PlayerUpdateEvent as LavalinkPlayerUpdateEvent
import dev.schlaubi.mikmusic.api.types.VoiceStateUpdateEvent as APIVoiceStateUpdateEvent

class StateWatcher : Extension() {
    override val name: String = "api state watcher"

    private val lavalinkManager by extension<LavalinkManager>()
    private val musicModule by extension<MusicModule>()

    override suspend fun setup() {
        lavalinkManager.onLoad {
            lavalinkManager.lavalink.on<TrackEndEvent> { sendEventFromEvent() }
            lavalinkManager.lavalink.on<TrackStartEvent> { sendEventFromEvent() }
            lavalinkManager.lavalink.on<LavalinkPlayerUpdateEvent> { sendEventFromEvent() }
        }
        handleVoiceStateUpdates()
        handleQueueUpdates()
    }

    private suspend fun Event.sendEventFromEvent() {
        val guildId = Snowflake(guildId)
        val guild = kord.getGuild(guildId)
        val player = musicModule.getCachedMusicPlayer(guildId) ?: return
        val time = (this as? LavalinkPlayerUpdateEvent)?.state?.time ?: System.currentTimeMillis()

        broadcastEvent(
            Snowflake(player.lastChannelId!!),
            PlayerUpdateEvent(player.toPlayerState(guild), player.queuedTracks.mapToAPIQueuedTrack(guild), time, guildId),
        )
    }

    private suspend fun handleVoiceStateUpdates() = event<VoiceStateUpdateEvent> {
        action {
            if (event.state.userId == kord.selfId) {
                val oldChannel = event.old?.channelId
                coroutineScope {
                    (getUsersInChannel(oldChannel) + getUsersInChannel(event.state.channelId)).forEach {
                        launch {
                            val voiceState = findVoiceState(event.state.guildId, it)
                            sendEventToUser(it, APIVoiceStateUpdateEvent(event.state.guildId, voiceState))
                        }
                    }
                }
            } else {
                val voiceState = event.state
                val userState = findVoiceState(voiceState.guildId, voiceState.userId)

                sendEventToUser(
                    voiceState.userId,
                    APIVoiceStateUpdateEvent(voiceState.guildId, userState)
                )
            }
        }
    }

    private fun handleQueueUpdates() = Queue.updates
        .onEach {
            val guildId = Snowflake(it.musicPlayer.guildId)
            val guild = kord.getGuild(guildId)
            val channel = it.musicPlayer.lastChannelId?:return@onEach

            broadcastEvent(
                Snowflake(channel),
                QueueUpdateEvent(it.tracks.mapToAPIQueuedTrack(guild), guildId)
            )
        }
        .launchIn(kord)

    private suspend fun findVoiceState(guildId: Snowflake, userId: Snowflake): Player.VoiceState {
        val result = kord.cache.query<VoiceStateData> {
            VoiceStateData::channelId ne null
            VoiceStateData::userId eq userId
        }.singleOrNull()

        val botState = musicModule.getCachedMusicPlayer(userId)
        val botChannel = botState?.lastChannelId?.let(::Snowflake)
        val playerAvailable = botState?.djModeRole == null
            || botState.djModeRole in kord.getGuild(guildId).getMember(userId).roleIds
        val botOffline = botChannel == null
        val userChannel = result?.channelId?.let { kord.getChannelOf<GuildChannel>(it) }?.toChannel()
        val channelMismatch = botChannel != result?.channelId

        return Player.VoiceState(
            channelMismatch,
            userChannel,
            botOffline || playerAvailable
        )
    }
}
