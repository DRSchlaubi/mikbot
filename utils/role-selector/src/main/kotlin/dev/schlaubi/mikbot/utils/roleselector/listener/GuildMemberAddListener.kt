package dev.schlaubi.mikbot.utils.roleselector.listener

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.rest.request.RestRequestException
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorModule

suspend fun RoleSelectorModule.guildMemeberAddListener() = event<MemberJoinEvent> {
    action {
        val role = RoleSelectorDatabase.autoRoleCollection.findOneById(event.guildId) ?: return@action
        try {
            event.member.addRole(role.roleId, "Auto Role")
        } catch (ex: RestRequestException) {
            event.guild.asGuild().owner.getDmChannel().createMessage {
                content = "Something went wrong while trying to give the set Auto Role to the new joining Member\n" +
                        "Error: ${ex.error?.message}"
            }
        }

    }
}
