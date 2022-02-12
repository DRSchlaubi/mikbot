package dev.schlaubi.mikbot.plugin.api.settings

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.kord.common.entity.Permission
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPointImpl
import dev.schlaubi.mikbot.plugin.api.config.Config

/**
 * Extension for settings.
 *
 * @see guildAdminOnly
 */
@OptIn(InternalAPI::class)
public abstract class SettingsModule @InternalAPI constructor() : ModuleExtensionPointImpl<SettingsModule>()

public interface SettingsExtensionPoint : ModuleExtensionPoint<SettingsModule> {
    public override suspend fun SettingsModule.apply()
}

public fun SlashCommand<*, *>.guildAdminOnly() {
    check {
        anyGuild()
        hasPermission(Permission.ManageGuild)
        if (event.interaction.user.id in Config.BOT_OWNERS) {
            pass() // bypass permission checks for bot owners
        }
    }
}

public fun SlashCommand<*, *>.botOwnerOnly() {
    check {
        failIfNot("You are not allowed to do this!") { event.interaction.user.id in Config.BOT_OWNERS }
    }
}
