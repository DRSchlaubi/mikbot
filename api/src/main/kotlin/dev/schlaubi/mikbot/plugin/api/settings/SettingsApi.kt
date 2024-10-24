package dev.schlaubi.mikbot.plugin.api.settings

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.kord.common.entity.ApplicationIntegrationType
import dev.kord.common.entity.InteractionContextType
import dev.kord.common.entity.Permission
import dev.schlaubi.mikbot.plugin.api.*

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
