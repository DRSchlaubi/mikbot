package dev.schlaubi.mikbot.plugin.api.settings

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.kord.common.entity.Permission
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPointImpl
import dev.schlaubi.mikbot.plugin.api.PluginSystem

/**
 * Extension for settings.
 *
 * @see guildAdminOnly
 */
@OptIn(InternalAPI::class)
public abstract class SettingsModule @InternalAPI constructor(pluginSystem: PluginSystem) :
    ModuleExtensionPointImpl<SettingsModule>(pluginSystem)

public interface SettingsExtensionPoint : ModuleExtensionPoint<SettingsModule> {
    public override suspend fun SettingsModule.apply()
}

public fun SlashCommand<*, *>.guildAdminOnly() {
    requirePermission(Permission.ManageGuild)
    allowInDms = false
}
