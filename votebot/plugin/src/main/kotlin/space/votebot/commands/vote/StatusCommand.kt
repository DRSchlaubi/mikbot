package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.poll
import space.votebot.core.VoteBotModule
import space.votebot.core.addMessage
import space.votebot.core.toEmbed

class StatusArguments : Arguments() {
    val poll by poll {
        name = "poll"
        description = "commands.status.arguments.poll.description"
    }

    val liveMessage by defaultingBoolean {
        name = "live"
        description = "commands.status.arguments.live.description"
        defaultValue = false
    }
}

suspend fun VoteBotModule.statusCommand() = ephemeralSlashCommand(::StatusArguments) {
    name = "status"
    description = "commands.status.description"

    action {
        val poll = arguments.poll
        if (!arguments.liveMessage) {
            respond {
                embeds.add(poll.toEmbed(channel.kord, guild!!))
            }
        } else {
            poll.addMessage(channel, addButtons = true, addToDatabase = true, guild = guild!!)

            respond {
                content = translate("commands.status.success")
            }
        }
    }
}
