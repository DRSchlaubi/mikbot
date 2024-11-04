package dev.schlaubi.mikmusic.lyrics

import dev.kordex.core.builders.ExtensionsBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule

@PluginMain
class LyricsPlugin(context: PluginContext) : Plugin(context) {
    override fun ExtensionsBuilder.addExtensions() {
        add(::LyricsModule)
    }
}

class LyricsModule(context: PluginContext) : MikBotModule(context) {
    override val name: String = "lyrics"

    override suspend fun setup() {
        lyricsCommand()
        karaokeCommand()
    }
}
