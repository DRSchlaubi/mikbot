package dev.schlaubi.mikbot.plugin.api.owner

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import dev.kord.common.entity.Permission
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
public abstract class OwnerModule @InternalAPI constructor() : ModuleExtensionPointImpl<OwnerModule>(), CoroutineScope

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
 * Configures this command, to only be usable by administrators of [Config.OWNER_GUILD].
 */
public fun SlashCommand<*, *>.ownerOnly() {
    guildId = Config.OWNER_GUILD ?: error("Cannot register owner command without OWNER_GUILD value")
    requirePermission(Permission.Administrator)
}
