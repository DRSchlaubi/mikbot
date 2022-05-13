package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.AllowedMentionType
import dev.kord.rest.builder.message.create.allowedMentions
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.setTranslationKey
import dev.schlaubi.mikbot.utils.roleselector.util.translateString
import dev.schlaubi.mikbot.utils.roleselector.util.updateMessage

suspend fun EphemeralSlashCommand<*>.removeRoleSelectionCommand() = ephemeralSubCommand(::RemoveRoleSelectionArguments) {
    name = "remove-role"
    description = "commands.remove_role.description"
    guildAdminOnly()
    setTranslationKey()

    action {
        val message = arguments.message
        val role = arguments.role

        val roleSelection = RoleSelectorDatabase.roleSelectionCollection.findOneById(message.id)
        if (roleSelection != null) {
            val roleSelectionButton = roleSelection.roleSelections.find { it.roleId == role.id }
            if (roleSelectionButton != null) {

                val newRoleSelectionMessage = RoleSelectionMessage(
                    message.id,
                    roleSelection.title,
                    roleSelection.description,
                    roleSelection.embedColor,
                    roleSelection.roleSelections.minus(
                        roleSelectionButton
                    )
                )

                RoleSelectorDatabase.roleSelectionCollection.save(newRoleSelectionMessage)

                updateMessage(message, newRoleSelectionMessage)

                respond {
                    content = translateString("commands.role_selection.message.role-removed", role.mention)
                    allowedMentions {
                        +AllowedMentionType.UserMentions
                    }
                }
            } else {
                respond {
                    content = translateString("commands.role_selection.message.no-role", role.mention)
                    allowedMentions {
                        +AllowedMentionType.UserMentions
                    }
                }
            }
        } else {
            respond {
                content = translateString("commands.role_selection.message.not-role-selection")
            }
        }
    }
}

class RemoveRoleSelectionArguments : Arguments() {
    val message by message {
        name = "message"
        description = "commands.remove_role.arguments.message.description"
    }
    val role by role {
        name = "role"
        description = "arguments.remove_role.arguments.role.description"
    }
}
