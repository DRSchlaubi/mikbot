package dev.schlaubi.musicbot.module.music.musicchannel

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
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
import dev.schlaubi.musicbot.module.music.checkOtherSchedulerOptions
import dev.schlaubi.musicbot.module.music.checks.joinSameChannelCheck
import dev.schlaubi.musicbot.module.music.checks.musicControlCheck
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.module.music.player.queue.findSpotifySongs
import dev.schlaubi.musicbot.module.settings.loop
import dev.schlaubi.musicbot.module.settings.playPause
import dev.schlaubi.musicbot.module.settings.repeatOne
import dev.schlaubi.musicbot.module.settings.shuffle
import dev.schlaubi.musicbot.module.settings.skip
import dev.schlaubi.musicbot.module.settings.stop
import dev.schlaubi.musicbot.utils.Confirmation
import dev.schlaubi.musicbot.utils.MessageBuilder
import dev.schlaubi.musicbot.utils.Translator
import dev.schlaubi.musicbot.utils.deleteAfterwards
import dev.schlaubi.musicbot.utils.extension
import dev.schlaubi.musicbot.utils.ifPassing
import dev.schlaubi.musicbot.utils.respondIfFailed
import org.koin.core.component.inject
import kotlin.reflect.KMutableProperty1

class MusicInteractionModule : Extension() {
    override val name = "music interaction handler"
    val database: Database by inject()
    val musicModule: MusicModule by extension()

    override suspend fun setup() {
        event<ComponentInteractionCreateEvent> {
            check {
                failIf {
                    val interaction = this.event.interaction
                    val message = interaction.message
                    val guild = message?.getGuild()
                    val guildSettings = guild?.let { database.guildSettings.findGuild(it) }

                    /* return */ interaction.message?.id != guildSettings?.musicChannelData?.musicChannelMessage
                }

                musicControlCheck()
                respondIfFailed()
            }

            action {
                val interaction = event.interaction
                val guild = interaction.message?.getGuildOrNull()!!
                val player = musicModule.getMusicPlayer(guild)
                val ack =
                    interaction.acknowledgeEphemeralDeferredMessageUpdate()

                suspend fun updateSchedulerOptions(
                    myProperty: KMutableProperty1<MusicPlayer, Boolean>,
                    vararg properties: KMutableProperty1<MusicPlayer, Boolean>
                ) {
                    ack.updateSchedulerOptions(
                        player,
                        { key, group -> translate(key, group) },
                        myProperty, *properties
                    )
                }

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
                        updateSchedulerOptions(
                            MusicPlayer::loopQueue,
                            MusicPlayer::shuffle, MusicPlayer::repeat
                        )
                    }
                    repeatOne -> {
                        updateSchedulerOptions(
                            MusicPlayer::repeat,
                            MusicPlayer::loopQueue, MusicPlayer::shuffle
                        )
                    }
                    shuffle -> {
                        updateSchedulerOptions(
                            MusicPlayer::shuffle,
                            MusicPlayer::loopQueue, MusicPlayer::repeat
                        )
                    }
                }

                return@action
            }
        }

        event<MessageCreateEvent> {
            check {
                // explicit false check here means also user != null, which avoids webhook messages
                if (event.message.author?.isBot != false) return@check fail()
                val guild = guildFor(event) ?: return@check fail()
                val channelId = database.guildSettings.findGuild(guild)
                    .musicChannelData?.musicChannel ?: return@check fail()

                inChannel(channelId)

                ifPassing { // only respond if this check fails
                    joinSameChannelCheck(bot)

                    respondIfFailed()
                }
            }

            action {
                val guild = guildFor(event) ?: return@action

                val player = musicModule.getMusicPlayer(guild)
                val tracks = player.takeFirstMatch(player, event.message.content)

                player.queueTrack(force = false, onTop = false, tracks = tracks)
                event.message.delete("Music channel interaction")

                if (tracks.isEmpty()) {
                    event.message.channel
                        .createMessage(translate("music.queue.no_matches"))
                        .deleteAfterwards()
                }
            }
        }
    }
}

suspend fun Link.takeFirstMatch(musicPlayer: MusicPlayer, query: String): List<Track> {
    val isUrl = query.startsWith("http")
    val queryString = if (isUrl) {
        query
    } else {
        "ytsearch: $query"
    }

    if (isUrl) {
        val spotifySearch = findSpotifySongs(musicPlayer, query)
        if (!spotifySearch.isNullOrEmpty()) {
            return spotifySearch
        }
    }

    val result = loadItem(queryString)
    return when (result.loadType) {
        TrackResponse.LoadType.TRACK_LOADED,
        TrackResponse.LoadType.PLAYLIST_LOADED -> result.tracks.mapToTrack()
        TrackResponse.LoadType.SEARCH_RESULT -> result.tracks.take(1).mapToTrack()
        else -> emptyList()
    }
}

private suspend fun EphemeralInteractionResponseBehavior.updateSchedulerOptions(
    musicPlayer: MusicPlayer,
    translate: Translator,
    myProperty: KMutableProperty1<MusicPlayer, Boolean>,
    vararg properties: KMutableProperty1<MusicPlayer, Boolean>
) = checkOtherSchedulerOptions(
    musicPlayer,
    translate,
    { messageBuilder ->
        confirmation(messageBuilder, translate)
    },
    { /* we just don't edit here because we don't need to */ },
    myProperty,
    properties = properties,
    translatorGroup = "settings",
    callback = {}
)

private suspend fun EphemeralInteractionResponseBehavior.confirmation(
    messageBuilder: MessageBuilder,
    translate: Translator
): Confirmation = dev.schlaubi.musicbot.utils.confirmation(
    {
        followUpEphemeral { it() }
    },
    messageBuilder = messageBuilder,
    translate = translate,
    hasNoOption = false
) // no option doesn't make a lot of sense here
