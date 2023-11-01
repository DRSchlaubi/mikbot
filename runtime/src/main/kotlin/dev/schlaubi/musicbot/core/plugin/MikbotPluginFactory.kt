package dev.schlaubi.musicbot.core.plugin

import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginSystem
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.musicbot.core.Bot
import mu.KotlinLogging
import org.pf4j.DefaultPluginFactory
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import kotlin.reflect.typeOf

private val LOG = KotlinLogging.logger { }

class MikbotPluginFactory(private val bot: Bot) : DefaultPluginFactory() {
    override fun createInstance(pluginClass: Class<*>, pluginWrapper: PluginWrapper): Plugin? {
        val newConstructor = pluginClass.kotlin.constructors.firstOrNull {
            it.parameters.size == 1 && it.parameters.first().type == typeOf<PluginContext>()
        }
        return if (newConstructor == null) {
            // legacy implementation
            super.createInstance(pluginClass, pluginWrapper)
        } else {
            try {
                val context = PluginContextImpl(bot.pluginSystem, bot.database, pluginWrapper)
                newConstructor.call(context) as Plugin
            } catch (e: Exception) {
                LOG.error("Could not instantiate plugin: ${pluginWrapper.pluginId}", e)
                null
            }
        }
    }
}

private class PluginContextImpl(
    override val pluginSystem: PluginSystem,
    override val database: Database,
    override val pluginWrapper: PluginWrapper,
) : PluginContext
