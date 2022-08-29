package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEmoji
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.AllowedMentionType
import dev.kord.rest.builder.message.create.allowedMentions
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionButton
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.*

suspend fun EphemeralSlashCommand<*>.addRoleSelectionCommand() = ephemeralSubCommand(::AddRoleSelectionArguments) {
    name = "add-role"
    description = "commands.add_role.description"
    guildAdminOnly()
    setTranslationKey()

    action {
        val message = arguments.message
        val role = arguments.role
        val label = arguments.label ?: role.name
        val emojiArg = arguments.emoji

        val oldRoleSelectionMessage = RoleSelectorDatabase.roleSelectionCollection.findOneById(message.id)

        if (oldRoleSelectionMessage == null) {
            respond {
                content = translateString("commands.error.not_a_role_selection_message")
            }
            return@action
        }

        if (!oldRoleSelectionMessage.roleSelections.any { it.roleId == role.id }) {
            val newRoleSelectionMessage = RoleSelectionMessage(
                message.id,
                safeGuild.id,
                oldRoleSelectionMessage.title,
                oldRoleSelectionMessage.description,
                oldRoleSelectionMessage.embedColor,
                oldRoleSelectionMessage.roleSelections.plus(
                    RoleSelectionButton(
                        role.id.value.toString(),
                        label,
                        emojiArg.toPartialEmoji(),
                        role.id
                    )
                )
            )

            RoleSelectorDatabase.roleSelectionCollection.save(newRoleSelectionMessage)

            updateMessage(message, newRoleSelectionMessage)

            respond {
                content = translateString("commands.role_selection.message.role-added", role.mention, label)
                allowedMentions {
                    +AllowedMentionType.UserMentions
                }
            }
        } else {
            val alreadyAssignedLabel = oldRoleSelectionMessage.roleSelections.find { it.roleId == role.id }?.label
            respond {
                content = translateString("commands.role_selection.message.role-already-added", role.mention, alreadyAssignedLabel)
                allowedMentions {
                    +AllowedMentionType.UserMentions
                }
            }
        }
    }
}

class AddRoleSelectionArguments : Arguments() {
    val message by message {
        name = "message"
        description = "commands.add_role.arguments.message.description"
        autoCompleteRoleSelectionMessage()
    }
    val role by role {
        name = "role"
        description = "commands.add_role.arguments.role.description"
    }
    val label by optionalString {
        name = "label"
        description = "commands.add_role.arguments.label.description"
    }
    val emoji by optionalEmoji {
        name = "emoji"
        description = "commands.add_role.arguments.emoji.description"
    }
}
