package dev.schlaubi.mikbot.plugin.api.owner

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.schlaubi.mikbot.plugin.api.InternalAPI
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPoint
import dev.schlaubi.mikbot.plugin.api.ModuleExtensionPointImpl
import dev.schlaubi.mikbot.plugin.api.config.Config
import kotlinx.coroutines.CoroutineScope

/**
 * Module which houses commands reserved for bot owners.
 *
 * @see ownerOnly
 */
@OptIn(InternalAPI::class)
public abstract class OwnerModule : ModuleExtensionPointImpl<OwnerModule>(), CoroutineScope

/**
 * The Extension point for Owner module customization.
 */
public interface OwnerExtensionPoint : ModuleExtensionPoint<OwnerModule> {
    /**
     * Applies instructions to the owner module.
     */
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
