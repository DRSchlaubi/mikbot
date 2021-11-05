package dev.schlaubi.mikbot.core.redeploy_hook

import dev.schlaubi.mikbot.core.redeploy_hook.api.RedeployExtensionPoint
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.owner.OwnerExtensionPoint
import dev.schlaubi.mikbot.plugin.api.owner.OwnerModule
import org.pf4j.Extension

@PluginMain
class RedeployHookPlugin(wrapper: PluginWrapper) : Plugin(wrapper)

@Extension
class RedeployHookOwnerExtension : OwnerExtensionPoint {
    override suspend fun OwnerModule.apply() {
        redeployCommand()
    }
}
