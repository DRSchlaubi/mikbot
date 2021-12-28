package dev.schlaubi.mikbot.util_plugins.botblock

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.request.KtorRequestException
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.time.Duration.Companion.minutes

private val LOG = KotlinLogging.logger { }

@PluginMain
class BotBlockPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::BotBlockExtension)
    }
}

class BotBlockExtension : Extension() {
    override val name: String = "botblock"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override suspend fun setup() {
        event<ReadyEvent> {
            startLoop()
        }
    }

    private fun startLoop() {
        scope.launch {
            delay(Config.BOTBLOCK_DELAY.minutes)
            try {
                kord.postStats(Config.BOT_LIST_TOKENS)
            } catch (e: KtorRequestException) {
                LOG.error(e) { "Could not post stats" }
            }
        }
    }

    override suspend fun unload() {
        scope.cancel()
    }
}
