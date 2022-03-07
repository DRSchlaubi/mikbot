package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.poll
import space.votebot.core.VoteBotModule
import space.votebot.core.close

class CloseArguments : Arguments() {
    val poll by poll {
        name = "poll"
        description = "The Message Link to the poll you want to close"
    }
    val showChart by optionalBoolean {
        name = "show-chart"
        description = "Whether to show a pie chart or a Discord message"
    }
}

suspend fun VoteBotModule.closeCommand() = ephemeralSlashCommand(::CloseArguments) {
    name = "close"
    description = "Closes a poll"

    action {
        arguments.poll.close(channel.kord, arguments.showChart, guild!!)

        respond {
            content = translate("commands.close.success")
        }
    }
}
