package dev.schlaubi.musicbot.module.music.musicchannel

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.mapToTrack
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.module.music.checks.joinSameChannelCheck
import dev.schlaubi.musicbot.module.music.checks.musicControlCheck
import dev.schlaubi.musicbot.module.settings.loop
import dev.schlaubi.musicbot.module.settings.playPause
import dev.schlaubi.musicbot.module.settings.repeatOne
import dev.schlaubi.musicbot.module.settings.shuffle
import dev.schlaubi.musicbot.module.settings.skip
import dev.schlaubi.musicbot.module.settings.stop
import dev.schlaubi.musicbot.utils.extension
import dev.schlaubi.musicbot.utils.respondIfFailed
import org.koin.core.component.inject

class MusicInteractionModule : Extension() {
    override val name = "music interaction handler"
    val database: Database by inject()
    val musicModule: MusicModule by extension()

    override suspend fun setup() {
        event<ComponentInteractionCreateEvent> {
            check {
                val interaction = this.event.interaction
                failIf {
                    if (interaction.message != null) {
                        val guildId = this.event.interaction.message?.getGuildOrNull()
                        if (guildId != null) {
                            val guildSettings = database.guildSettings.findGuild(guildId)

                            if (interaction.message!!.id != guildSettings.musicChannelData?.musicChannelMessage) {
                                return@failIf true
                            }

                            return@failIf false
                        }
                    }

                    true
                }

                musicControlCheck()

                respondIfFailed()
            }

            action {
                val interaction = this.event.interaction

                val guildId = this.event.interaction.message?.getGuildOrNull()
                if (guildId != null) {
                    val player = musicModule.getMusicPlayer(guildId)

                    when (interaction.componentId) {
                        playPause -> {
                            player.player.pause(!player.player.paused)
                        }
                        stop -> {
                            player.stop()
                        }
                        skip -> {
                            player.skip()
                        }
                        loop -> {
                            player.loopQueue = !player.loopQueue
                        }
                        repeatOne -> {
                            player.repeat = !player.repeat
                        }
                        shuffle -> {
                            player.shuffle = !player.shuffle
                        }
                    }

                    interaction.acknowledgeEphemeralDeferredMessageUpdate()
                    return@action
                }
            }
        }

        event<MessageCreateEvent> {
            check {
                anyGuild()
                val channelId = database.guildSettings.findGuild(
                    guildFor(event) ?: return@check fail()
                ).musicChannelData?.musicChannel ?: return@check fail()
                inChannel(channelId)

                joinSameChannelCheck(bot)

                respondIfFailed()
            }

            action {
                val guild = guildFor(event) ?: return@action

                val tracks = musicModule.getMusicPlayer(guild).takeFirstMatch(event.message.content)

                musicModule.getMusicPlayer(guild).queueTrack(force = false, onTop = false, tracks = tracks)

                event.message.delete()
            }
        }
    }
}

suspend fun Link.takeFirstMatch(query: String): List<Track> {
    val queryString = if (query.startsWith("http")) {
        query
    } else {
        "ytsearch: $query"
    }

    val result = loadItem(queryString)
    return when (result.loadType) {
        TrackResponse.LoadType.TRACK_LOADED, TrackResponse.LoadType.PLAYLIST_LOADED
        -> result.tracks.take(1).mapToTrack()
        TrackResponse.LoadType.SEARCH_RESULT -> result.tracks.take(1).mapToTrack()
        else -> emptyList()
    }
}
