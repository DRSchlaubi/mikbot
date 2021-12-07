package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule

suspend fun SettingsModule.createRoleMessageCommand() = ephemeralSlashCommand {
    name = "role-selector"
    description = "<never used>"

    addRoleMessageCommand()
    removeRoleMessageCommand()
    addRoleSelectionCommand()
    removeRoleSelectionCommand()
    editRoleSelectionCommand()
}







