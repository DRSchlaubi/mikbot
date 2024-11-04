package dev.schlaubi.mikmusic.musicchannel

import dev.kord.common.asJavaLocale
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.TranslatableContext
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.player.ChapterQueuedTrack
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.addAutoPlaySongs
import dev.schlaubi.mikmusic.util.addSong
import dev.schlaubi.mikmusic.util.format
import kotlinx.datetime.Clock
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
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

suspend fun updateMessage(
    guildId: Snowflake,
    kord: Kord,
    musicPlayer: MusicPlayer,
    initialUpdate: Boolean = false,
    translator: TranslationsProvider,
) {
    val message = findMessageSafe(guildId, kord)
    val locale = kord.getGuildOrNull(guildId)?.preferredLocale?.convertToISO()?.asJavaLocale()
        ?: translator.defaultLocale


    @Suppress("UNCHECKED_CAST")
    fun translate(key: Key, vararg replacements: Any?) =
        translator.translate(key.withLocale(locale, overwrite = true), replacements as Array<Any?>)

    val translatableContext = object : TranslatableContext {
        override var resolvedLocale: Locale? = locale
        override suspend fun getLocale(): Locale = locale
    }

    message?.edit {
        if (initialUpdate) {
            // Clear initial loading text
            // This requires the content to be explicitly an empty string
            // afterwards we will just send null (or effectively no value) to shrink down the request size
            content = ""
        }
        val playingQueueTrack = musicPlayer.playingTrack
        embed {
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

                if (musicPlayer.queuedTracks.isNotEmpty()) {
                    field {
                        name = translate(MusicTranslations.Music.MusicChannel.nextSongAt)
                        value = if (musicPlayer.player.paused) {
                            translate(MusicTranslations.Music.MusicChannel.paused)
                        } else {
                            "<t:${nextSongAt.epochSeconds}:R>"
                        }
                    }
                }
            }

            title = translate(MusicTranslations.Music.MusicChannel.queue)
            description = musicPlayer.queuedTracks.take(5).mapIndexed { index, track -> track to index }
                .joinToString("\n") { (track, index) ->
                    (index + 1).toString() + ". " + track.format()
                }.ifBlank { translate(MusicTranslations.Music.MusicChannel.Queue.empty) }

            musicPlayer.addAutoPlaySongs(translatableContext)

            footer {
                text = translate(MusicTranslations.Music.MusicChannel.footer)
            }
        }

        actionRow {
            musicButton(
                musicPlayer,
                playPause,
                Emojis.playPause,
                enabled = musicPlayer.player.paused,
                enabledStyle = ButtonStyle.Danger
            )
            musicButton(musicPlayer, stop, Emojis.stopButton, ButtonStyle.Danger)
            musicButton(
                musicPlayer,
                skip,
                Emojis.nextTrack,
                // You cannot skip, if there is no next item in the queue
                additionalCondition = musicPlayer.canSkip
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
                enabled = musicPlayer.autoPlay != null
            )
        }
        actionRow {
            musicButton(
                musicPlayer,
                loop,
                Emojis.repeat,
                enabled = musicPlayer.loopQueue
            )
            musicButton(
                musicPlayer,
                repeatOne,
                Emojis.repeatOne,
                enabled = musicPlayer.repeat
            )
            musicButton(
                musicPlayer,
                shuffle,
                Emojis.twistedRightwardsArrows,
                enabled = musicPlayer.shuffle
            )
        }
    }
}

suspend fun findMessageSafe(guildId: Snowflake, kord: Kord): Message? {
    val guildSettings = MusicSettingsDatabase.guild.findOneById(guildId)
    val (channelId, messageId) = guildSettings?.musicChannelData ?: return null

    val message = kord.getGuildOrNull(guildId)?.getChannelOfOrNull<TextChannel>(channelId)?.getMessageOrNull(messageId)

    // if the message is not found, disable the feature
    if (message == null) {
        MusicSettingsDatabase.guild.save(guildSettings.copy(musicChannelData = null))
    }

    return message
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
