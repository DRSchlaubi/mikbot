package dev.schlaubi.mikmusic.musicchannel

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.asJavaLocale
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.TranslatableContext
import dev.schlaubi.lavakord.RestException
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import dev.schlaubi.mikbot.plugin.api.util.embed
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.api.types.ChapterQueuedTrack
import dev.schlaubi.mikmusic.api.types.QueuedTrack
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.player.AutoPlayContext
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.addAutoPlaySongs
import dev.schlaubi.mikmusic.util.addSong
import dev.schlaubi.mikmusic.util.format
import kotlin.time.Clock
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val playPause = "playPause"
const val stop = "stop"
const val skip = "skip"
const val autoPlay = "auto_play"
const val skipChapter = "skip_chapter"
const val loop = "loop"
const val repeatOne = "repeatOne"
const val shuffle = "shuffle"

data class MusicChannelInformation(
    val playingTrack: QueuedTrack?,
    val position: Long,
    val queuedTracks: List<QueuedTrack>,
    val paused: Boolean,
    val canSkip: Boolean,
    val autoPlayContext: AutoPlayContext?,
    val shuffle: Boolean,
    val loopQueue: Boolean,
    val repeat: Boolean,
) {
    companion object {
        fun from(player: MusicPlayer) = MusicChannelInformation(
            player.playingTrack,
            player.player.position,
            player.queuedTracks,
            player.player.paused,
            player.canSkip,
            player.autoPlay,
            player.shuffle,
            player.loopQueue,
            player.repeat
        )
    }
}

private val cache = mutableMapOf<Snowflake, MusicChannelInformation>()

@OptIn(KordExperimental::class, KordUnsafe::class)
suspend fun updateMessage(
    guildId: Snowflake,
    kord: Kord,
    musicPlayer: MusicPlayer,
    initialUpdate: Boolean = false,
    translator: TranslationsProvider,
    force: Boolean = false,
) {
    val state = MusicChannelInformation.from(musicPlayer)
    val existingState = cache[guildId]
    if (existingState == state && !force) return // Do not send useless request if status is identical
    cache[guildId] = state
    val settings = MusicSettingsDatabase.guild.findOneById(guildId) ?: return
    val musicChannelData = settings.musicChannelData ?: return
    val message = kord.unsafe.message(musicChannelData.musicChannel, musicChannelData.musicChannelMessage)

    val locale = kord.getGuildOrNull(guildId)?.preferredLocale?.convertToISO()?.asJavaLocale()
        ?: translator.defaultLocale

    @Suppress("UNCHECKED_CAST")
    fun translate(key: Key, vararg replacements: Any?) =
        translator.translate(key.withLocale(locale, overwrite = true), replacements as Array<Any?>)

    val translatableContext = object : TranslatableContext {
        override var resolvedLocale: Locale? = locale
        override suspend fun getLocale(): Locale = locale
    }

    val playingQueueTrack = state.playingTrack

    val embedBuilder = embed {
        val playingTrack = playingQueueTrack?.track
        if (playingTrack != null) {
            addSong(translatableContext, playingTrack)

            if (playingQueueTrack is ChapterQueuedTrack) {
                field {
                    name = translate(MusicTranslations.Music.MusicChannel.chapter)
                    value = playingQueueTrack.chapters[playingQueueTrack.chapterIndex].name
                }
            }

            val remainingTime = playingTrack.info.length
                .toDuration(DurationUnit.MILLISECONDS) - musicPlayer.player.position.milliseconds
            val nextSongAt = Clock.System.now() + remainingTime

            if (state.queuedTracks.isNotEmpty()) {
                field {
                    name = translate(MusicTranslations.Music.MusicChannel.nextSongAt)
                    value = if (state.paused) {
                        translate(MusicTranslations.Music.MusicChannel.paused)
                    } else {
                        "<t:${nextSongAt.epochSeconds}:R>"
                    }
                }
            }
        }

        title = translate(MusicTranslations.Music.MusicChannel.queue)
        description = state.queuedTracks.take(5).mapIndexed { index, track -> track to index }
            .joinToString("\n") { (track, index) ->
                (index + 1).toString() + ". " + track.format()
            }.ifBlank { translate(MusicTranslations.Music.MusicChannel.Queue.empty) }

        state.autoPlayContext.addAutoPlaySongs(translatableContext)

        footer {
            text = translate(MusicTranslations.Music.MusicChannel.footer)
        }
    }

    val row1 = ActionRowBuilder().apply {
        musicButton(
            musicPlayer,
            playPause,
            Emojis.playPause,
            enabled = state.paused,
            enabledStyle = ButtonStyle.Danger
        )
        musicButton(musicPlayer, stop, Emojis.stopButton, ButtonStyle.Danger)
        musicButton(
            musicPlayer,
            skip,
            Emojis.nextTrack,
            // You cannot skip, if there is no next item in the queue
            additionalCondition = state.canSkip
        )
        if (playingQueueTrack is ChapterQueuedTrack) {
            musicButton(
                musicPlayer,
                skipChapter,
                Emojis.fastForward,
                additionalCondition = !playingQueueTrack.isOnLast
            )
        }
        musicButton(
            musicPlayer,
            autoPlay,
            Emojis.blueCar,
            enabled = state.autoPlayContext != null
        )
    }
    val row2 = ActionRowBuilder().apply {
        musicButton(
            musicPlayer,
            loop,
            Emojis.repeat,
            enabled = state.loopQueue
        )
        musicButton(
            musicPlayer,
            repeatOne,
            Emojis.repeatOne,
            enabled = state.repeat
        )
        musicButton(
            musicPlayer,
            shuffle,
            Emojis.twistedRightwardsArrows,
            enabled = state.shuffle
        )
    }

    val buttons = mutableListOf<MessageComponentBuilder>(row1, row2)

    if (Clock.System.now() - message.id.timestamp > 30.minutes) {
        message.delete("Music channel refresh")
        val newMessage = message.channel.createMessage {
            embeds = mutableListOf(embedBuilder)
            components = buttons
        }
        MusicSettingsDatabase.guild.save(settings.copy(musicChannelData = musicChannelData.copy(musicChannelMessage = newMessage.id)))
    } else {
        try {
            message.edit {
                if (initialUpdate) {
                    // Clear initial loading text
                    // This requires the content to be explicitly an empty string
                    // afterwards we will just send null (or effectively no value) to shrink down the request size
                    content = ""
                }

                embeds = mutableListOf(embedBuilder)
                components = buttons
            }
        } catch (_: RestException) {
            MusicSettingsDatabase.guild.save(settings.copy(musicChannelData = null))
        }
    }
}

private fun ActionRowBuilder.musicButton(
    musicPlayer: MusicPlayer,
    name: String,
    emoji: DiscordEmoji,
    buttonStyle: ButtonStyle = ButtonStyle.Primary,
    additionalCondition: Boolean = true,
    enabled: Boolean = false,
    enabledStyle: ButtonStyle = ButtonStyle.Success,
) {
    val playingCondition = musicPlayer.playingTrack != null && musicPlayer.savedTrack == null

    val style = if (enabled && playingCondition) {
        enabledStyle
    } else {
        buttonStyle
    }

    interactionButton(style, name) {
        this.emoji = DiscordPartialEmoji(name = emoji.unicode)

        disabled = !(playingCondition && additionalCondition)
    }
}
