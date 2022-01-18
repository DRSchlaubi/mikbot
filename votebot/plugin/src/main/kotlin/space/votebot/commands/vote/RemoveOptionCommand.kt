package space.votebot.commands.vote

import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import space.votebot.command.PollArguments
import space.votebot.command.poll
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase
import space.votebot.core.VoteBotModule
import space.votebot.core.recalculateEmojis
import space.votebot.core.updateMessages
import space.votebot.transformer.TransformerContext
import space.votebot.transformer.transformMessage

class RemoveOptionArguments : PollArguments("The poll you want to remove the option from") {
    val position by int {
        name = "position"
        description = "The position at which the option should be inserted"
    }
}

suspend fun VoteBotModule.removeOptionCommand() = ephemeralSlashCommand(::RemoveOptionArguments) {
    name = "remove-option"
    description = "Adds an option to a poll"

    action {
        val poll = poll()
        val selectedOption = poll.sortedOptions[arguments.position - 1]
        val newOptions = poll.options.mapIndexed { index, option ->
            if (index == selectedOption.index) {
                Poll.Option.Spacer(null)
            } else {
                option
            }
        }
        val newVotes = poll.votes.filterNot { (forOption) ->
            forOption == selectedOption.index
        }
        val newPoll = poll.copy(options = newOptions, votes = newVotes).recalculateEmojis(safeGuild)

        VoteBotDatabase.polls.save(newPoll)
        newPoll.updateMessages(channel.kord, guild!!)
        respond {
            content = translate(
                "commands.remove_option.success",
                arrayOf(
                    transformMessage(
                        selectedOption.option,
                        TransformerContext(guild!!, this@removeOptionCommand.kord, true)
                    )
                )
            )
        }
    }
}
