package dev.schlaubi.mikbot.utils.roleselector.command.roleselection

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.utils.roleselector.util.setTranslationKey

suspend fun SettingsModule.createRoleMessageCommand() = ephemeralSlashCommand {
    name = "role-selector"
    description = "<never used>"
    guildAdminOnly()
    setTranslationKey()

    addRoleMessageCommand()
    removeRoleMessageCommand()
    editRoleMessageCommand()

    addRoleSelectionCommand()
    removeRoleSelectionCommand()
    editRoleSelectionCommand()
}
