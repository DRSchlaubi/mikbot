package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.message
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalColor
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.autoCompleteRoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.util.setTranslationKey
import dev.schlaubi.mikbot.utils.roleselector.util.translateString
import dev.schlaubi.mikbot.utils.roleselector.util.updateMessage

suspend fun SlashCommand<*, *>.editRoleMessageCommand() = ephemeralSubCommand(::EditRoleMessageArguments) {
    name = "edit-message"
    description = "commands.edit_message.description"

    guildAdminOnly()
    setTranslationKey()

    action {
        val message = arguments.message

        val oldSelectionMessage = RoleSelectorDatabase.roleSelectionCollection.findOneById(message.id)

        if (oldSelectionMessage == null) {
            respond {
                content = translateString("commands.role_selection.message.not-role-selection")
            }
            return@action
        }

        val embedTitle = arguments.title ?: oldSelectionMessage.title
        val embedDescription = arguments.description ?: oldSelectionMessage.description
        val embedColor = arguments.embedColor ?: oldSelectionMessage.embedColor
        val multiple = arguments.multiple ?: oldSelectionMessage.multiple
        val showSelections = arguments.showSelections ?: oldSelectionMessage.showSelections

        val newRoleSelectionMessage = oldSelectionMessage.copy(
            guildId = safeGuild.id,
            title = embedTitle,
            description = embedDescription,
            embedColor = embedColor,
            multiple = multiple,
            showSelections = showSelections
        )

        updateMessage(message, newRoleSelectionMessage)

        RoleSelectorDatabase.roleSelectionCollection.save(newRoleSelectionMessage)

        respond {
            content = translateString("commands.role_selection.message.edited")
        }
    }
}

class EditRoleMessageArguments : Arguments() {
    val message by message {
        name = "message"
        description = "commands.edit_message.arguments.message.description"
        autoCompleteRoleSelectionMessage()
    }
    val title by optionalString {
        name = "title"
        description = "commands.create_message.arguments.title.description"
    }
    val description by optionalString {
        name = "description"
        description = "commands.create_message.arguments.description.description"
    }
    val embedColor by optionalColor {
        name = "color"
        description = "commands.create_message.arguments.color.description"
    }
    val multiple by optionalBoolean {
        name = "multiple"
        description = "commands.create_message.arguments.multiple.description"
    }
    val showSelections by optionalBoolean {
        name = "show-selections"
        description = "commands.create_message.arguments.show-selections.description"
    }
}
