@file:Suppress("UnnecessaryOptInAnnotation") // Inspection is a false-positiev

package dev.schlaubi.mikmusic.musicchannel

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalChannel
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.TranslationsProvider
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.settings.MusicChannelData
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.util.musicModule
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import org.koin.core.component.get

private class MusicChannelArguments : Arguments() {
    val channel by optionalChannel {
        name = MusicTranslations.Commands.Music_channel.Arguments.Channel.name
        description = MusicTranslations.Commands.Music_channel.Arguments.Channel.description

        validate {
            val channel = value ?: return@validate
            val botPermissions = (channel.fetchChannel() as TextChannel).getEffectivePermissions(channel.kord.selfId)
            if (Permission.ManageMessages !in botPermissions) {
                discordError(MusicTranslations.Command.Music_channel.channel_missing_perms)
            }
        }
        requiredChannelTypes.add(ChannelType.GuildText)
    }
}

@OptIn(KordUnsafe::class, KordExperimental::class)
suspend fun SettingsModule.musicChannel() {
    ephemeralSlashCommand(::MusicChannelArguments) {
        name = MusicTranslations.Commands.Music_channel.name
        description = MusicTranslations.Commands.Music_channel.description

        guildAdminOnly()

        action {
            val guildSettings = MusicSettingsDatabase.findGuild(safeGuild)

            if (arguments.channel == null) {
                val confirmation = confirmation {
                    content = translate(MusicTranslations.Settings.Musicchannel.Reset.confirm)
                }

                if (confirmation.value) {
                    MusicSettingsDatabase.guild.save(guildSettings.copy(musicChannelData = null))
                    if (guildSettings.musicChannelData != null)
                        user.kord.unsafe.message(
                            guildSettings.musicChannelData.musicChannel,
                            guildSettings.musicChannelData.musicChannelMessage
                        ).delete()
                    confirmation.edit {
                        content = translate(MusicTranslations.Settings.Musicchannel.Reset.done)
                    }
                }

                return@action
            }

            if (guildSettings.musicChannelData != null) {
                val (confirmed) = confirmation {
                    content = translate(MusicTranslations.Settings.Musicchannel.confirmnew)
                }

                if (!confirmed) {
                    edit { content = translate(MusicTranslations.Settings.Musicchannel.New.aborted) }
                    return@action
                }
            }

            val textChannel = (arguments.channel!!.fetchChannel() as TextChannel)
                // disable the cache for this one, because message caching has issues
                .withStrategy(EntitySupplyStrategy.rest)
                .fetchChannel()

            if (textChannel.getLastMessage() != null) {
                val (confirmed) = confirmation {
                    content = translate(MusicTranslations.Settings.Musicchannel.try_delete_messages)
                }

                if (confirmed) {
                    val messages = textChannel
                        .withStrategy(EntitySupplyStrategy.rest)
                        .messages
                        .map { it.id }
                        .toSet()
                    textChannel.bulkDelete(messages)
                }
            }

            val message = textChannel.createMessage {
                content = translate(MusicTranslations.Settings.loading)
            }

            message.pin("Main music channel message")

            MusicSettingsDatabase.guild.save(
                guildSettings.copy(
                    musicChannelData = MusicChannelData(textChannel.id, message.id)
                )
            )

            // Remove loading text
            updateMessage(
                safeGuild.id,
                this@ephemeralSlashCommand.kord,
                musicModule.getMusicPlayer(safeGuild),
                true,
                get<TranslationsProvider>()
            )

            respond {
                content = translate(MusicTranslations.Settings.Musicchannel.createdchannel)
            }
        }
    }
}
