package dev.schlaubi.mikmusic.musicchannel

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.followUpEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.mapToTrack
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.util.*
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.checks.musicControlCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.checkOtherSchedulerOptions
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.queue.findSpotifySongs
import dev.schlaubi.mikmusic.util.mapToQueuedTrack
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
                    val guild = message.getGuild()
                    val guildSettings = guild.let { MusicSettingsDatabase.findGuild(guild) }

                    /* return */ interaction.message.id != guildSettings.musicChannelData?.musicChannelMessage
                }

                musicControlCheck()
                respondIfFailed()
            }

            action {
                val interaction = event.interaction
                val guild = interaction.message.getGuildOrNull()!!
                val player = musicModule.getMusicPlayer(guild)
                val ack =
                    interaction.deferEphemeralMessageUpdate()

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
                    playPause -> player.pause()
                    stop -> player.stop()
                    skip -> player.skip()
                    skipChapter -> player.skipChapter()
                    loop -> updateSchedulerOptions(
                        MusicPlayer::loopQueue,
                        MusicPlayer::shuffle, MusicPlayer::repeat
                    )
                    repeatOne -> updateSchedulerOptions(
                        MusicPlayer::repeat,
                        MusicPlayer::loopQueue, MusicPlayer::shuffle
                    )
                    shuffle -> updateSchedulerOptions(
                        MusicPlayer::shuffle,
                        MusicPlayer::loopQueue, MusicPlayer::repeat
                    )
                }

                return@action
            }
        }

        event<MessageCreateEvent> {
            check {
                // explicit false check here means also user != null, which avoids webhook messages
                if (event.message.author?.isBot != false) return@check fail()
                val guild = guildFor(event) ?: return@check fail()
                val channelId = MusicSettingsDatabase.findGuild(guild)
                    .musicChannelData?.musicChannel ?: return@check fail()

                inChannel(channelId)

                ifPassing { // only respond if this check fails
                    failIf(translate("music.music_channel.disabled", "music")) {
                        val player = musicModule.getMusicPlayer(guild)
                        player.disableMusicChannel
                    }

                    joinSameChannelCheck(bot)
                    respondIfFailed()
                }
            }

            action {
                event.message.channel.withTyping {
                    val guild = event.getGuild()!!
                    event.message.delete()
                    val player = musicModule.getMusicPlayer(guild)
                    val tracks = player.takeFirstMatch(
                        player,
                        event.message.content
                    )

                    player.queueTrack(
                        force = false,
                        onTop = false,
                        tracks = tracks.mapToQueuedTrack(event.message.author!!)
                    )

                    if (tracks.isEmpty()) {
                        event.message
                            .reply { content = translate("music.queue.no_matches", "music") }
                            .deleteAfterwards()
                    }
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
        val spotifySearch: List<Track>? = findSpotifySongs(musicPlayer, query)

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

private suspend fun EphemeralMessageInteractionResponseBehavior.updateSchedulerOptions(
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

private suspend fun EphemeralMessageInteractionResponseBehavior.confirmation(
    messageBuilder: MessageBuilder,
    translate: Translator
): Confirmation = confirmation(
    {
        createEphemeralFollowup {
            it()
        }
    },
    messageBuilder = messageBuilder,
    translate = translate,
    hasNoOption = false
) // no option doesn't make a lot of sense here
