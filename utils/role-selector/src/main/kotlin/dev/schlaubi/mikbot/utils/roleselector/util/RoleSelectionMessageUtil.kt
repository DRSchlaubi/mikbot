package dev.schlaubi.mikbot.utils.roleselector.util

import com.kotlindiscord.kord.extensions.commands.converters.impl.MessageConverterBuilder
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import org.litote.kmongo.eq

suspend fun updateMessage(message: Message, roleSelectionMessage: RoleSelectionMessage) {
    message.edit {
        embed {
            title = roleSelectionMessage.title
            val embedDescription =
                StringBuilder(roleSelectionMessage.description ?: "").apply {
                    if (isNotEmpty() && roleSelectionMessage.showSelections) {
                        appendLine()
                        appendLine()
                    }
                }
            if (roleSelectionMessage.showSelections) {
                roleSelectionMessage.roleSelections.forEach {
                    it.emoji?.let { (id, name, animated) ->
                        embedDescription.append("<${if (animated.orElse(false)) "a" else ""}:${name}:${id}> ")
                    }
                    embedDescription.append(it.label).appendLine().appendLine()
                }
            }
            description = embedDescription.toString()
            color = roleSelectionMessage.embedColor
        }
        roleSelectionMessage.roleSelections.chunked(5) {
            actionRow {
                it.forEach {
                    interactionButton(
                        ButtonStyle.Primary,
                        it.buttonId
                    ) {
                        this.emoji = it.emoji
                        this.label = it.label
                    }
                }
            }
        }
    }
}

fun MessageConverterBuilder.autoCompleteRoleSelectionMessage() {
    autoComplete {
        val channel = channel.asChannelOf<TextChannel>()
        val roleSelectorMessages = RoleSelectorDatabase.roleSelectionCollection
            .find(RoleSelectionMessage::guildId eq channel.guildId).toList()

        suggestString {
            roleSelectorMessages.forEach {
                choice(it.title, buildMessageLink(channel.guildId, channel.id, it.messageId))
            }
        }
    }
}

private fun buildMessageLink(guild: Snowflake, channel: Snowflake, message: Snowflake) =
    "https://discord.com/channels/$guild/$channel/$message"
