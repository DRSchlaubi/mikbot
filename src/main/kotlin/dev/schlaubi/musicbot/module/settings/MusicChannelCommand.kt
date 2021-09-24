package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.edit
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.musicbot.core.io.Database
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.utils.confirmation
import dev.schlaubi.musicbot.utils.format
import dev.schlaubi.musicbot.utils.safeGuild

private class MusicChannelArguments : Arguments() {
    val musicChannel by channel("channel", "Text Channel to use for Music Channel", validator = { _, value ->
        if (value.type != ChannelType.GuildText) {
            throw DiscordRelayedException(translate("commands.musicchannel.notextchannel", arrayOf(value.data.name)))
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

        check {
            anyGuild()
            hasPermission(Permission.ManageGuild)
        }

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

            val message = (arguments.musicChannel as TextChannel).createMessage {
                content = "loading..."
            }

            database.guildSettings.save(
                guildSettings.copy(
                    musicChannelData = MusicChannelData(arguments.musicChannel.id, message.id)
                )
            )

            musicModule.getMusicPlayer(safeGuild).updateMusicChannelMessage()

            respond {
                content = translate("settings.musicchannel.createdchannel")
            }
        }
    }
}

@OptIn(KordUnsafe::class, dev.kord.common.annotation.KordExperimental::class)
suspend fun updateMessage(guildId: Snowflake, database: Database, kord: Kord, musicPlayer: MusicPlayer) {
    val (channelId, messageId) = database.guildSettings.findOneById(guildId)?.musicChannelData ?: return

    kord.unsafe.message(channelId, messageId).edit {
        embed {
            title = "Queue"
            description = musicPlayer.queuedTracks.take(5).mapIndexed { index, track -> track to index }
                .joinToString("\n") { (track, index) ->
                    (index + 1).toString() + ". " + track.format()
                }.ifBlank { "No further songs in queue" }
        }

        actionRow {
            musicButton(musicPlayer, playPause, Emojis.playPause)
            musicButton(musicPlayer, stop, Emojis.stopButton, ButtonStyle.Danger)
            musicButton(
                musicPlayer,
                skip,
                Emojis.fastForward,
                customDisabledCondition = musicPlayer.queuedTracks.isEmpty() || musicPlayer.player.playingTrack == null
            )
        }
        actionRow {
            musicButton(
                musicPlayer,
                loop,
                Emojis.repeat,
                if (musicPlayer.loopQueue) ButtonStyle.Success else ButtonStyle.Primary
            )
            musicButton(
                musicPlayer,
                repeatOne,
                Emojis.repeatOne,
                if (musicPlayer.repeat) ButtonStyle.Success else ButtonStyle.Primary
            )
            musicButton(
                musicPlayer,
                shuffle,
                Emojis.twistedRightwardsArrows,
                if (musicPlayer.shuffle) ButtonStyle.Success else ButtonStyle.Primary
            )
        }
    }
}

private fun ActionRowBuilder.musicButton(
    musicPlayer: MusicPlayer,
    name: String,
    emoji: DiscordEmoji,
    buttonStyle: ButtonStyle = ButtonStyle.Primary,
    customDisabledCondition: Boolean? = null
) {
    interactionButton(buttonStyle, name) {
        this.emoji = DiscordPartialEmoji(name = emoji.unicode)

        disabled =
            customDisabledCondition ?: (musicPlayer.player.playingTrack == null)
    }
}
