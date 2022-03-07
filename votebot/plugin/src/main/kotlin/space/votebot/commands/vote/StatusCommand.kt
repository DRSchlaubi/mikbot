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
        description = "The Message Link to the poll you want to see the status of"
    }

    val liveMessage by defaultingBoolean {
        name = "live"
        description = "Whether you want this to be a message, also accepting votes or not"
        defaultValue = false
    }
}

suspend fun VoteBotModule.statusCommand() = ephemeralSlashCommand(::StatusArguments) {
    name = "status"
    description = "Displays the status of a Poll"

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
