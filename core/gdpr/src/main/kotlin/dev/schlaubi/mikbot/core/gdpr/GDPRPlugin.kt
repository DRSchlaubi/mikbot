package dev.schlaubi.mikbot.core.gdpr

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain

@PluginMain
class GDPRPlugin(wrapper: PluginContext) : Plugin(wrapper) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::GDPRModule)
    }
}
