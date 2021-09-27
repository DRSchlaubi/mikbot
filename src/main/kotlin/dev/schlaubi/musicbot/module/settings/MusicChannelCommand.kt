package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.utils.addSong
import dev.schlaubi.musicbot.utils.confirmation
import dev.schlaubi.musicbot.utils.format
import dev.schlaubi.musicbot.utils.safeGuild
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlin.time.Duration

private class MusicChannelArguments : Arguments() {
    val channel by channel("channel", "Text Channel to use for Music Channel", validator = { _, value ->
        if (value.type != ChannelType.GuildText) {
            throw DiscordRelayedException(translate("commands.musicchannel.notextchannel", arrayOf(value.data.name)))
        }

        val botPermissions = (value as TextChannel).getEffectivePermissions(value.kord.selfId)
        if (Permission.ManageMessages !in botPermissions) {
            throw DiscordRelayedException(translate("command.music_channel.chnnal_missing_perms"))
        }
    })
}

const val playPause = "playPause"
const val stop = "stop"
const val skip = "skip"
const val loop = "loop"
const val repeatOne = "repeatOne"
const val shuffle = "shuffle"

suspend fun SettingsModule.musicChannel() {
    ephemeralSlashCommand(::MusicChannelArguments) {
        name = "music-channel"
        description = "Set your music channel in this guild"

        guildAdminOnly()

        action {
            val guildSettings = database.guildSettings.findGuild(safeGuild)

            if (guildSettings.musicChannelData != null) {
                val (confirmed) = confirmation {
                    content = translate("settings.musicchannel.confirmnew")
                }

                if (!confirmed) {
                    edit { content = translate("settings.musicchannel.new.aborted") }
                    return@action
                }
            }

            val textChannel = (arguments.channel as TextChannel)
                // disable the cache for this one, because message caching has issues
                .withStrategy(EntitySupplyStrategy.rest)

            if (textChannel.getLastMessage() != null) {
                val (confirmed) = confirmation {
                    content = translate("settings.musicchannel.try_delete_messages")
                }

                if (confirmed) {
                    val messages = textChannel
                        .messages
                        .map { it.id }
                        .toList()
                    textChannel.bulkDelete(messages)
                }
            }

            val message = (arguments.channel as TextChannel).createMessage {
                content = translate("settings.loading")
            }

            message.pin("Main music channel message")

            database.guildSettings.save(
                guildSettings.copy(
                    musicChannelData = MusicChannelData(arguments.channel.id, message.id)
                )
            )

            // Remove loading text
            updateMessage(
                safeGuild.id,
                database,
                this@ephemeralSlashCommand.kord,
                musicModule.getMusicPlayer(safeGuild),
                true,
                translationsProvider
            )

            respond {
                content = translate("settings.musicchannel.createdchannel")
            }
        }
    }
}

@OptIn(KordUnsafe::class, dev.kord.common.annotation.KordExperimental::class)
suspend fun updateMessage(
    guildId: Snowflake,
    database: Database,
    kord: Kord,
    musicPlayer: MusicPlayer,
    initialUpdate: Boolean = false,
    translationsProvider: TranslationsProvider
) {
    findMessageSafe(database, guildId, kord)?.edit {
        if (initialUpdate) {
            // Clear initial loading text
            // This requires the content to be explicitly an empty string
            // afterwards we will just send null (or effectively no value) to shrink down the request size
            content = ""
        }
        embed {
            val playingTrack = musicPlayer.player.playingTrack
            if (playingTrack != null) {
                addSong({ key, group -> translationsProvider.translate(key, bundleName = group) }, playingTrack)

                val remainingTime = playingTrack.length - Duration.Companion.milliseconds(musicPlayer.player.position)
                val nextSongAt = Clock.System.now() + remainingTime

                if (musicPlayer.queuedTracks.isNotEmpty()) {

                    field {
                        name = "Next song at"
                        if (musicPlayer.player.paused) {
                            value = "You will never reach the next song at this speed. (Bot is paused)"
                        } else {
                            value = "<t:${nextSongAt.epochSeconds}:R>"
                        }
                    }
                }

            }

            title = "Queue"
            description = musicPlayer.queuedTracks.take(5).mapIndexed { index, track -> track to index }
                .joinToString("\n") { (track, index) ->
                    (index + 1).toString() + ". " + track.track.format()
                }.ifBlank { "No further songs in queue" }

            footer {
                text = "Send a message in this channel to queue a song"
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
                Emojis.fastForward,
                // You cannot skip, if there is no next item in the queue
                additionalCondition = musicPlayer.queuedTracks.isNotEmpty()
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

suspend fun findMessageSafe(database: Database, guildId: Snowflake, kord: Kord): Message? {
    val guildSettings = database.guildSettings.findOneById(guildId)
    val (channelId, messageId) = guildSettings?.musicChannelData ?: return null

    val message = kord.getGuild(guildId)?.getChannelOfOrNull<TextChannel>(channelId)?.getMessageOrNull(messageId)

    // if the message is not found, disable the feature
    if (message == null) {
        database.guildSettings.save(guildSettings.copy(musicChannelData = null))
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
    enabledStyle: ButtonStyle = ButtonStyle.Success
) {
    val playingCondition = musicPlayer.player.playingTrack != null

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
