package dev.schlaubi.mikbot.utils.roleselector.util

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage

suspend fun updateMessage(message: Message, roleSelectionMessage: RoleSelectionMessage) {
    message.edit {
        embed {
            title = roleSelectionMessage.title
            val embedDescription =
                StringBuilder(roleSelectionMessage.description ?: "").apply {
                    if(isNotEmpty()) {
                        appendLine()
                        appendLine()
                    }
                }
            roleSelectionMessage.roleSelections.forEach {
                it.emoji?.let { emoji ->
                    embedDescription.append("<${emoji.name}:${emoji.id}> ")
                }
                embedDescription.append(it.label).appendLine().appendLine()
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
