package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.setTranslationKey
import dev.schlaubi.mikbot.utils.roleselector.util.translateString

suspend fun EphemeralSlashCommand<*>.removeRoleMessageCommand() = ephemeralSubCommand(::RemoveRoleMessageArguments) {
    name = "remove-message"
    description = "commands.remove_message.description"
    guildAdminOnly()
    setTranslationKey()

    action {
        val message = arguments.message

        if (RoleSelectorDatabase.roleSelectionCollection.deleteOneById(message.id).deletedCount > 0) {
            message.delete(translateString("commands.role_selection.message.removed"))
            respond {
                content = translateString("commands.role_selection.message.removed")
            }
        } else {
            respond {
                content = translateString("commands.role_selection.message.not-role-selection")
            }
        }
    }
}

class RemoveRoleMessageArguments : Arguments() {
    val message by message {
        name = "message"
        description = "commands.remove_message.arguments.message.description"
    }
}
