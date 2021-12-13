package space.votebot.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.poll
import space.votebot.core.VoteBotDatabase
import space.votebot.core.VoteBotModule
import space.votebot.core.close
import space.votebot.core.findByMessage

class CloseArguments : Arguments() {
    val poll by poll()
    val showChart by optionalBoolean("show-chart", "Whether to show a pie chart or a Discord message")
}

suspend fun VoteBotModule.closeCommand() = ephemeralSlashCommand(::CloseArguments) {
    name = "close"
    description = "Closes a poll"

    action {
        val poll = VoteBotDatabase.polls.findByMessage(arguments.poll)
        if (poll == null) {
            respond {
                content = translate("commands.generic.poll_bot_found")
            }
            return@action
        }

        poll.close(channel.kord, arguments.showChart)
    }
}
