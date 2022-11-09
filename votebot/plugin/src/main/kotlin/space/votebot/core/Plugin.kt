package space.votebot.core

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.config.Environment
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.cancel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import org.litote.kmongo.serialization.registerSerializer
import space.votebot.commands.commands

@PluginMain
class VoteBotPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    @OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
    override fun start() {
        registerSerializer(ULong.serializer())
    }

    @OptIn(PrivilegedIntent::class)
    override suspend fun ExtensibleBotBuilder.apply() {
        if (Config.ENVIRONMENT == Environment.PRODUCTION) {
            kord {
                httpClient = HttpClient(CIO) {
                    engine {
                        threadsCount = 25
                    }
                }
            }
        }
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::VoteBotModule)
    }

    override fun stop() {
        ExpirationScope.cancel()
    }
}

class VoteBotModule : Extension() {
    override val name: String = "votebot"
    override val bundle: String = "votebot"
    override val allowApplicationCommandInDMs: Boolean = false

    override suspend fun setup() {
        commands()
        voteExecutor()

        event<ReadyEvent> {
            action {
                rescheduleAllPollExpires(kord)
            }
        }
    }
}
