package dev.schlaubi.mikmusic.lyrics

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.module.MikBotModule

@PluginMain
class LyricsPlugin(context: PluginContext) : Plugin(context) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::LyricsModule)
    }
}

class LyricsModule(context: PluginContext) : MikBotModule(context) {
    override val name: String = "lyrics"
    override val bundle: String = "lyrics"

    override suspend fun setup() {
        lyricsCommand()
        karaokeCommand()
    }
}
