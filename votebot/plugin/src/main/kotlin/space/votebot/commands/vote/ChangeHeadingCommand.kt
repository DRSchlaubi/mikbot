package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.poll
import space.votebot.core.VoteBotModule
import space.votebot.core.updateMessages

class ChangeHeadingArguments : Arguments() {
    val poll by poll {
        name = "poll"
        description = "commands.change_heading.arguments.poll.description"
    }

    val heading by string {
        name = "new-heading"
        description = "commands.change_heading.arguments.new_heading.description"
    }
}

suspend fun VoteBotModule.changeHeadingCommand() = ephemeralSlashCommand(::ChangeHeadingArguments) {
    name = "change-heading"
    description = "commands.change_heading.description"

    action {
        val poll = arguments.poll
        val newPoll = poll.copy(title = arguments.heading)
        respond {
            content = translate("commands.change_heading.success", arrayOf(newPoll.title))
        }

        if (newPoll.title != poll.title) {
            newPoll.updateMessages(channel.kord, guild!!)
        }
    }
}
