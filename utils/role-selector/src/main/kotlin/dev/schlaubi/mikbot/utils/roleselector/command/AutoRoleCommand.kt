package dev.schlaubi.mikbot.utils.roleselector.command

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalRole
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.selfMember
import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.create.allowedMentions
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.utils.roleselector.AutoRole
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.util.translateString
import kotlinx.coroutines.flow.toList

suspend fun SettingsModule.autoRoleCommand() = ephemeralSlashCommand(::AutoRoleArguments) {
    name = "auto-role"
    description = "Set the role to give when a User join the Guild or disable it"
    guildAdminOnly()
    requireBotPermissions(Permission.ManageRoles)

    action {
        if (arguments.role == null) {
            if (RoleSelectorDatabase.autoRoleCollection.deleteOneById(safeGuild.id).deletedCount > 0) {
                respond {
                    content = translateString("commands.autorole.disabled")
                }
            } else {
                respond {
                    content = translateString("commands.autorole.already-disabled")
                }
            }
        } else {
            RoleSelectorDatabase.autoRoleCollection.save(
                AutoRole(
                    safeGuild.id,
                    arguments.role!!.id
                )
            )
            respond {
                content = translateString("commands.autorole.role-set", arguments.role!!.mention)
                allowedMentions {
                    +AllowedMentionType.UserMentions
                }
            }
        }
    }
}

class AutoRoleArguments : Arguments() {
    val role by optionalRole {
        name = "role"
        description = "The Role to give (The bot's role has to be above the role that should be given)"
        validate {
            val member = context.getMember() ?: return@validate
            val highestBotRole = context.getGuild()!!.selfMember().roles.toList().maxOfOrNull {
                it.getPosition()
            } ?: 0
            if (value!!.getPosition() >= highestBotRole) {
                throw DiscordRelayedException(translateString("error.commands.missing-permission.role.bot"))
            }
            if (member.asMember().isOwner()) return@validate
            val highestMemberRole = member.asMember().roles.toList().maxOfOrNull {
                it.getPosition()
            } ?: 0
            if (value!!.getPosition() >= highestMemberRole) {
                throw DiscordRelayedException(translateString("error.commands.missing-permission.role.member"))
            }
        }
    }
}
