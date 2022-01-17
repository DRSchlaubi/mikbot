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
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.rest.builder.message.create.allowedMentions
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionButton
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.translateString
import dev.schlaubi.mikbot.utils.roleselector.util.updateMessage

suspend fun EphemeralSlashCommand<*>.addRoleSelectionCommand() = ephemeralSubCommand(::AddRoleSelectionArguments) {
    name = "add-role"
    description = "Adds a new Role-Selection to a Role-Selection Message"
    guildAdminOnly()

    action {
        val message = arguments.message
        val role = arguments.role
        val label = arguments.label ?: role.name
        val emoji = arguments.emoji

        val oldRoleSelectionMessage = RoleSelectorDatabase.roleSelectionCollection.findOneById(message.id)!!

        if (!oldRoleSelectionMessage.roleSelections.any { it.roleId == role.id }) {
            val newRoleSelectionMessage = RoleSelectionMessage(
                message.id,
                oldRoleSelectionMessage.title,
                oldRoleSelectionMessage.description,
                oldRoleSelectionMessage.embedColor,
                oldRoleSelectionMessage.roleSelections.plus(
                    RoleSelectionButton(
                        role.id.value.toString(),
                        label,
                        if (emoji != null) DiscordPartialEmoji(
                            id = emoji.id,
                            name = emoji.name,
                            animated = emoji.isAnimated.optional()
                        ) else null,
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
        description = "The Role-Selection Message to add a Role-Selection to"
    }
    val role by role {
        name = "role"
        description = "The Role to add to the Role-Selection"
    }
    val label by optionalString {
        name = "label"
        description = "The String that shows in the Embed and on the Button"
    }
    val emoji by optionalEmoji {
        name = "emoji"
        description = "The Custom Emoji that should appear in-front the label (Put default emojis in the label)"
    }
}
