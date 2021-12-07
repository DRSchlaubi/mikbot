package dev.schlaubi.mikbot.utils.roleselector

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.settings.SettingsExtensionPoint
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.utils.roleselector.command.autoRoleCommand
import dev.schlaubi.mikbot.utils.roleselector.command.roleselection.createRoleMessageCommand
import dev.schlaubi.mikbot.utils.roleselector.listener.guildMemeberAddListener
import dev.schlaubi.mikbot.utils.roleselector.listener.interactionCreateListener
import org.pf4j.Extension
import com.kotlindiscord.kord.extensions.extensions.Extension as KordExtension

@PluginMain
class RoleSelectorPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::RoleSelectorModule)
    }
}

@Extension
class RoleSelectorSettingsExtension: SettingsExtensionPoint {
    override suspend fun SettingsModule.apply() {
        autoRoleCommand()
        createRoleMessageCommand()
    }
}

class RoleSelectorModule: KordExtension() {
    override val name: String = "role selector event handler"

    override suspend fun setup() {
        guildMemeberAddListener()
        interactionCreateListener()
    }
}
