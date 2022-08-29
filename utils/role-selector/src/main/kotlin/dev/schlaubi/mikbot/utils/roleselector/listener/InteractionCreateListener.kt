package dev.schlaubi.mikbot.utils.roleselector.listener

import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.request.RestRequestException
import dev.schlaubi.mikbot.plugin.api.util.respondEphemeral
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectionButton
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorModule
import dev.schlaubi.mikbot.utils.roleselector.util.translateString
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

suspend fun RoleSelectorModule.interactionCreateListener() = event<ComponentInteractionCreateEvent> {
    action {
        val interaction = event.interaction

        val messageId = interaction.message.id
        val selector = RoleSelectorDatabase.roleSelectionCollection.findOneById(messageId) ?: return@action
        val roleId = Snowflake(interaction.componentId)
        if (selector.roleSelections.none { it.roleId == roleId }) return@action

        val guild = interaction.message.getGuildOrNull() ?: return@action
        val member = interaction.user.asMember(guild.id)
        val role = guild.getRole(roleId)

        if (selector.guildId == null) {
            RoleSelectorDatabase.roleSelectionCollection
                .updateOneById(messageId, selector.copy(guildId = guild.id))
        }

        try {
            if (!member.hasRole(role)) {
                if (!selector.multiple) {
                    member.roles.onEach {
                        if (selector.roleSelections.map(RoleSelectionButton::roleId).contains(it.id)) {
                            member.removeRole(it.id)
                        }
                    }.collect()
                }
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
