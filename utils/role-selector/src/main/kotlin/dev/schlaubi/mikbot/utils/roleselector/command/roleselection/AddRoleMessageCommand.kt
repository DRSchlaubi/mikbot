package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalColor
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionMessage
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.setTranslationKey
import dev.schlaubi.mikbot.utils.roleselector.util.translateString

suspend fun EphemeralSlashCommand<*>.addRoleMessageCommand() = ephemeralSubCommand(::CreateRoleMessageArguments) {
    name = "create-message"
    description = "commands.create_message.description"
    guildAdminOnly()
    setTranslationKey()

    action {
        val embedTitle = arguments.title
        val embedDescription = arguments.description
        val embedColor = arguments.embedColor
        val sendingChannel =
            arguments.channel?.asChannelOfOrNull() ?: channel.asChannelOfOrNull<TopGuildMessageChannel>()
        val multiple = arguments.multiple ?: true
        val showSelections = arguments.showSelections ?: true

        if (sendingChannel == null) {
            respond {
                content = translate("commands.role_selector.add_message.invalid_channel")
            }
            return@action
        }

        if (Permissions(Permission.SendMessages, Permission.EmbedLinks) !in
            sendingChannel.getEffectivePermissions(sendingChannel.kord.selfId)
        ) {
            respond {
                content = translate("commands.role_selector.add_message.missing_permission")
            }
        }

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
                safeGuild.id,
                embedTitle,
                embedDescription,
                embedColor,
                emptyList(),
                multiple,
                showSelections
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
    val channel by optionalChannel {
        name = "channel"
        description = "commands.create_message.arguments.channel.description"
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
