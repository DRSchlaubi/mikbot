package dev.schlaubi.mikbot.plugin.api.settings

import dev.kord.common.entity.InteractionContextType
import dev.kord.common.entity.Permission
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPointImpl
import dev.schlaubi.mikbot.plugin.api.PluginContext

/**
 * Extension for settings.
 *
 * @see guildAdminOnly
 */
@OptIn(InternalAPI::class)
public abstract class SettingsModule @InternalAPI constructor(context: PluginContext) :
    ModuleExtensionPointImpl<SettingsModule>(context)

public interface SettingsExtensionPoint : ModuleExtensionPoint<SettingsModule> {
    public override suspend fun SettingsModule.apply()
}

public fun SlashCommand<*, *, *>.guildAdminOnly() {
    requirePermission(Permission.ManageGuild)
    allowedContexts.add(InteractionContextType.Guild)
}
