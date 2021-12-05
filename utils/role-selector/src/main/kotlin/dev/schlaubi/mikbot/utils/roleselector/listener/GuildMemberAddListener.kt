package dev.schlaubi.mikbot.utils.roleselector.listener

import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.guild.MemberJoinEvent
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorDatabase
import dev.schlaubi.mikbot.utils.roleselector.RoleSelectorModule

suspend fun RoleSelectorModule.guildMemeberAddListener() = event<MemberJoinEvent> {
    action {
        val role = RoleSelectorDatabase.autoRoleCollection.findOneById(event.guildId) ?: return@action
        event.member.addRole(role.roleId, "Auto Role")
    }
}