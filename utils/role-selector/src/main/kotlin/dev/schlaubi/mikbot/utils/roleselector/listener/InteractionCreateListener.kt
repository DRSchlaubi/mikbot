package dev.schlaubi.mikbot.utils.roleselector.listener

import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.request.RestRequestException
import dev.schlaubi.mikbot.plugin.api.util.respondEphemeral
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorModule
import dev.schlaubi.mikbot.utils.roleselector.util.translateString

suspend fun RoleSelectorModule.interactionCreateListener() = event<ComponentInteractionCreateEvent> {
    action {
        val interaction = event.interaction

        if (
            interaction.message != null &&
            RoleSelectorDatabase.roleSelectionCollection.findOneById(interaction.message!!.channel.id) != null
        ) return@action

        val guild = interaction.message?.getGuildOrNull()!!
        val member = interaction.user.asMember(guild.id)
        val role = guild.getRole(Snowflake(interaction.componentId))

        try {
            if (!member.hasRole(role)) {
                member.addRole(role.id, reason = "Role-Selection")
                interaction.respondEphemeral {
                    content = translateString("interaction.role_selection.role_assigned", role.mention)
                    allowedMentions {
                        +AllowedMentionType.UserMentions
                    }
                }
            } else {
                member.removeRole(role.id, reason = "Role-Selection")
                interaction.respondEphemeral {
                    content = translateString("interaction.role_selection.role_unassigned", role.mention)
                    allowedMentions {
                        +AllowedMentionType.UserMentions
                    }
                }
            }
        } catch (ex: RestRequestException) {
            interaction.respondEphemeral {
                content = translateString("error.general.user", ex.message)
            }
        }
    }
}
