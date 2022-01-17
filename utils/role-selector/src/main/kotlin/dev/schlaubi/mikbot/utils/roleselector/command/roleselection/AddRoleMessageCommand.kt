package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalColor
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.translateString

suspend fun EphemeralSlashCommand<*>.addRoleMessageCommand() = ephemeralSubCommand(::CreateRoleMessageArguments) {
    name = "create-message"
    description = "Create a new Role Selection Message"
    guildAdminOnly()

    action {
        val embedTitle = arguments.title
        val embedDescription = arguments.description
        val embedColor = arguments.embedColor
        val sendingChannel = arguments.channel?.asChannelOf() ?: channel.asChannel()

        val message = sendingChannel.createMessage {
            embed {
                title = embedTitle
                description = embedDescription
                color = embedColor
            }
        }

        RoleSelectorDatabase.roleSelectionCollection.save(
            RoleSelectionMessage(
                message.id,
                embedTitle,
                embedDescription,
                embedColor,
                emptyList()
            )
        )

        respond {
            content = translateString("commands.role_selection.message.created")
        }
    }
}

class CreateRoleMessageArguments : Arguments() {
    val title by string {
        name = "title"
        description = "The Title of the Message Embed"
    }
    val description by optionalString {
        name = "description"
        description = "The description of this role selection Embed"
    }
    val embedColor by optionalColor {
        name = "color"
        description = "The Color of the Role-Selection Embed"
    }
    val channel by optionalChannel {
        name = "channel"
        description = "The Channel to send the message to (if not given, current channel will be used)"
    }
}
