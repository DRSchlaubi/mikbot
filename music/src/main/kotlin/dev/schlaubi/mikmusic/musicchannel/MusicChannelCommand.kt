package dev.schlaubi.mikmusic.musicchannel

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.settings.MusicChannelData
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.util.musicModule
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet

private class MusicChannelArguments : Arguments() {
    val channel by channel("channel", "Text Channel to use for Music Channel", validator = { _, value ->
        if (value.type != ChannelType.GuildText) {
            throw DiscordRelayedException(translate("commands.musicchannel.notextchannel", arrayOf(value.data.name)))
        }

        val botPermissions = (value.fetchChannel() as TextChannel).getEffectivePermissions(value.kord.selfId)
        if (Permission.ManageMessages !in botPermissions) {
            throw DiscordRelayedException(translate("command.music_channel.channel_missing_perms"))
        }
    })
}

suspend fun SettingsModule.musicChannel() {
    ephemeralSlashCommand(::MusicChannelArguments) {
        name = "music-channel"
        description = "Set your music channel in this guild"

        guildAdminOnly()

        action {
            val guildSettings = MusicSettingsDatabase.findGuild(safeGuild)

            if (guildSettings.musicChannelData != null) {
                val (confirmed) = confirmation {
                    content = translate("settings.musicchannel.confirmnew")
                }

                if (!confirmed) {
                    edit { content = translate("settings.musicchannel.new.aborted") }
                    return@action
                }
            }

            val textChannel = (arguments.channel.fetchChannel() as TextChannel)
                // disable the cache for this one, because message caching has issues
                .withStrategy(EntitySupplyStrategy.rest)
                .fetchChannel()

            if (textChannel.getLastMessage() != null) {
                val (confirmed) = confirmation {
                    content = translate("settings.musicchannel.try_delete_messages")
                }

                if (confirmed) {
                    val messages = textChannel
                        .messages
                        .map { it.id }
                        .toSet() + setOfNotNull(textChannel.lastMessageId)
                    textChannel.bulkDelete(messages)
                }
            }

            val message = textChannel.createMessage {
                content = translate("settings.loading")
            }

            message.pin("Main music channel message")

            MusicSettingsDatabase.guild.save(
                guildSettings.copy(
                    musicChannelData = MusicChannelData(arguments.channel.id, message.id)
                )
            )

            // Remove loading text
            updateMessage(
                safeGuild.id,
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
