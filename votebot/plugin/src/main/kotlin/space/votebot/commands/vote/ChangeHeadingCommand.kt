package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.PollArguments
import space.votebot.command.poll
import space.votebot.core.VoteBotModule
import space.votebot.core.updateMessages

class ChangeHeadingArguments : PollArguments("The Message Link to the poll you want to change the heading of") {
    val heading by string {
        name = "new-heading"
        description = "The new heading"
    }
}

suspend fun VoteBotModule.changeHeadingCommand() = ephemeralSlashCommand(::ChangeHeadingArguments) {
    name = "change-heading"
    description = "Changes the heading of the Poll"

    action {
        val poll = poll()
        val newPoll = poll.copy(title = arguments.heading)
        respond {
            content = translate("commands.change_heading.success", arrayOf(newPoll.title))
        }

        if (newPoll.title != poll.title) {
            newPoll.updateMessages(channel.kord, guild!!)
        }
    }
}
