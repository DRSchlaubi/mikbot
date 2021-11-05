package dev.schlaubi.mikmusic.redeploy

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.schlaubi.mikbot.core.redeploy_hook.api.RedeployExtensionPoint
import dev.schlaubi.mikmusic.core.MusicModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.pf4j.Extension

@Extension
class RedeployExtension : RedeployExtensionPoint, KoinComponent {
    val bot by inject<ExtensibleBot>()
    override suspend fun beforeRedeploy() {
        val musicModule = bot.findExtension<MusicModule>()!!

        musicModule.savePlayerStates()
        musicModule.disconnect()
    }
}
