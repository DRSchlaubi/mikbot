package space.votebot.core

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import space.votebot.commands.create.createCommands

@PluginMain
class VoteBotPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::VoteBotModule)
    }
}

class VoteBotModule : Extension() {
    override val name: String = "votebot"

    override suspend fun setup() {
        slashCommandCheck { anyGuild() }

        createCommands()
        voteExecutor()
    }
}
