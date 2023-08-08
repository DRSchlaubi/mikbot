package dev.schlaubi.mikmusic.core

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.nycode.imagecolor.ImageColorClient
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikmusic.core.audio.LavalinkManager
import dev.schlaubi.mikmusic.musicchannel.MusicInteractionModule
import dev.schlaubi.mikmusic.util.JsonObjectSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.litote.kmongo.serialization.registerSerializer

@PluginMain
class MusicPlugin(wrapper: PluginContext) : Plugin(wrapper) {

    override fun start() {
        registerSerializer(JsonObjectSerializer)
    }
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::LavalinkManager)
        add(::MusicModule)
        if (Config.ENABLE_MUSIC_CHANNEL_FEATURE) {
            add(::MusicInteractionModule)
        }
    }

    override fun CoroutineScope.atLaunch(bot: ExtensibleBot) {
        launch {
            bot.findExtension<LavalinkManager>()!!.load()
        }
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            beforeKoinSetup {
                val imageColorServiceUrl = Config.IMAGE_COLOR_SERVICE_URL
                if (imageColorServiceUrl != null) {
                    loadModule {
                        single { ImageColorClient(imageColorServiceUrl) }
                    }
                }
            }
        }
    }
}
