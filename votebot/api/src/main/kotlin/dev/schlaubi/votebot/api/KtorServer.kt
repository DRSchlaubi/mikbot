package dev.schlaubi.votebot.api

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.Kord
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginContext
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.votebot.api.controllers.mainController
import dev.schlaubi.votebot.api.error.installErrorHandler
import dev.schlaubi.votebot.api.oauth.installDiscordAuth
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import org.koin.core.component.inject
import org.pf4j.Extension

@Extension
class KtorServer : KtorExtensionPoint, KordExKoinComponent {
    val kord by inject<Kord>()
    override fun Application.apply() {
        installDiscordAuth()

        mainController()
    }

    override fun StatusPagesConfig.apply() = installErrorHandler()
}

@PluginMain
class VoteBotApiPlugin(context: PluginContext) : Plugin(context)
