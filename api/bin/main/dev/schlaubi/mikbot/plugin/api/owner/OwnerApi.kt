package dev.schlaubi.mikbot.plugin.api.owner

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPointImpl
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import kotlinx.coroutines.CoroutineScope

@OptIn(InternalAPI::class)
public abstract class OwnerModule : ModuleExtensionPointImpl<OwnerModule>(), CoroutineScope

public interface OwnerExtensionPoint : ModuleExtensionPoint<OwnerModule> {
    public override suspend fun OwnerModule.apply()
}

/**
 * Configures this command, to be only usable by [Config.BOT_OWNERS].
 */
public fun SlashCommand<*, *>.ownerOnly() {
    allowByDefault = false
    if (Config.OWNER_GUILD != null) {
        guildId = Config.OWNER_GUILD!!
    }
    Config.BOT_OWNERS.forEach(::allowUser)
}
