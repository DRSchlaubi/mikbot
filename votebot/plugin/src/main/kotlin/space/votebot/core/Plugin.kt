package space.votebot.core

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.kord.core.event.gateway.ReadyEvent
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import kotlinx.coroutines.cancel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import org.litote.kmongo.serialization.registerSerializer
import space.votebot.command.legacyCommandParser
import space.votebot.commands.commands

@PluginMain
class VoteBotPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    @OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)
    override fun start() {
        registerSerializer(ULong.serializer())
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

    override suspend fun setup() {
        slashCommandCheck { anyGuild() }

        commands()
        voteExecutor()
        legacyCommandParser()

        event<ReadyEvent> {
            action {
                rescheduleAllPollExpires(kord)
            }
        }
    }
}
