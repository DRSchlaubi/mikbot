package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.PollArguments
import space.votebot.command.poll
import space.votebot.core.VoteBotModule
import space.votebot.core.addMessage
import space.votebot.core.toEmbed

class StatusArguments : PollArguments("The Message Link to the poll you want to see the status of") {
    val liveMessage by defaultingBoolean(
        "live",
        "Whether you want this to be a message, also accepting votes or not",
        false
    )
}

suspend fun VoteBotModule.statusCommand() = ephemeralSlashCommand(::StatusArguments) {
    name = "status"
    description = "Displays the status of a Poll"

    action {
        val poll = poll()
        if (!arguments.liveMessage) {
            respond {
                embeds.add(poll.toEmbed(channel.kord))
            }
        } else {
            poll.addMessage(channel, addButtons = true, addToDatabase = true)

            respond {
                content = translate("commands.status.success")
            }
        }
    }
}
