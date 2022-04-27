package dev.schlaubi.mikbot.utils.roleselector.listener

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberUpdateEvent
import dev.kord.rest.request.RestRequestException
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorModule

suspend fun RoleSelectorModule.guildMemberAddListener() = event<MemberJoinEvent> {
    action {
        if (event.member.isPending) return@action

        addRole(event.member, event.getGuild())
    }
}

suspend fun RoleSelectorModule.guildMemberUpdateListener() = event<MemberUpdateEvent> {
    action {
        if (event.old?.isPending == true && !event.member.isPending) {
            addRole(event.member, event.getGuild())
        }
    }
}

private suspend fun addRole(member: Member, guild: Guild) {
    val role = RoleSelectorDatabase.autoRoleCollection.findOneById(guild.id) ?: return
    try {
        member.addRole(role.roleId, "Auto Role")
    } catch (ex: RestRequestException) {
        guild.asGuild().owner.getDmChannel().createMessage {
            content = "Something went wrong while trying to give the set Auto Role to the new joining Member\n" +
                    "Error: ```${ex.error?.message}```"
        }
    }
}