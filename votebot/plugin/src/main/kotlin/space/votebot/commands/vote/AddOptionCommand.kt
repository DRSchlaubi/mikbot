package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import space.votebot.command.PollArguments
import space.votebot.command.poll
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase
import space.votebot.core.VoteBotModule
import space.votebot.core.updateMessages

class AddOptionArguments : PollArguments("The poll you want to add the argument to") {
    val option by string("option", "The option you want to add")
    val position by optionalInt("position", "The position at which the option should be inserted")
}

suspend fun VoteBotModule.addOptionCommand() = ephemeralSlashCommand(::AddOptionArguments) {
    name = "add-option"
    description = "Adds an option to a poll"

    action {
        val poll = poll()
        val newPoll = poll.copy(
            options = poll.options + Poll.Option.ActualOption(
                arguments.position?.minus(1),
                arguments.option
            )
        )

        VoteBotDatabase.polls.save(newPoll)
        newPoll.updateMessages(channel.kord)
        respond {
            content = translate("commands.add_option.success", arrayOf(arguments.option))
        }
    }
}
