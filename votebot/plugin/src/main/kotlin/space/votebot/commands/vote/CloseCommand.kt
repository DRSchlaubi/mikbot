package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.PollArguments
import space.votebot.command.poll
import space.votebot.core.VoteBotModule
import space.votebot.core.close

class CloseArguments : PollArguments("The Message Link to the poll you want to close") {
    val showChart by optionalBoolean("show-chart", "Whether to show a pie chart or a Discord message")
}

suspend fun VoteBotModule.closeCommand() = ephemeralSlashCommand(::CloseArguments) {
    name = "close"
    description = "Closes a poll"

    action {
        poll().close(channel.kord, arguments.showChart)

        respond {
            content = translate("commands.close.success")
        }
    }
}
